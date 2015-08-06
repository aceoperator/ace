package com.quikj.server.app;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.quikj.client.raccess.AceRMIImpl;
import com.quikj.client.raccess.AceRMIInterface;
import com.quikj.server.app.adapter.PolledAppServerAdapter;
import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceTimer;

public class ApplicationServer implements ApplicationContextAware {

	private static ApplicationServer instance = null;

	private String hostName;

	private MBeanServer mbeanServer;

	private Map<String, ObjectName> registeredObjects = new HashMap<String, ObjectName>();

	private ApplicationContext context;

	private Registry registry;

	private static String remoteServiceName;

	private static AceRMIInterface aceRmi;

	public ApplicationServer() {
		try {
			hostName = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			hostName = "Unknown";
		}
		instance = this;
	}

	public static ApplicationServer getInstance() {
		return instance;
	}

	private void initApps() {
		// start all the applications
		int[] applications = PluginAppList.Instance().listApplications();
		for (int i = 0; i < applications.length; i++) {
			try {
				PluginAppList.Instance().initApplication(applications[i]);
			} catch (AceException e) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"ApplicationServer.ApplicationServer() -- "
								+ " failed to start application id "
								+ applications[i], e);
			}
		}

		// Register to RMI service
		new CommunicatorClientList();

		initMbean();
	}

	private void initMbean() {
		mbeanServer = ManagementFactory.getPlatformMBeanServer();
		registerMbean(EndPointManagementMBean.MBEAN_NAME,
				new EndPointManagement());
	}

	public void registerMbean(String name, Object obj) {
		try {
			ObjectName objName = new ObjectName(name);
			mbeanServer.registerMBean(obj, objName);
			registeredObjects.put(name, objName);
		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"ApplicationServer.registerMbean() -- Could not register MBean - "
							+ e.getMessage(), e);
		}
	}

	public void unregisterMbean(String name) {
		try {
			ObjectName obj = registeredObjects.get(name);
			if (obj != null) {
				mbeanServer.unregisterMBean(obj);
				registeredObjects.remove(obj);
			}
		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"ApplicationServer.unregisterMbean() -- Could not register MBbean - "
							+ e.getMessage(), e);
		}
	}

	public void startup() throws UnknownHostException, BeansException,
			AceException {
		ApplicationConfiguration config = context
				.getBean(ApplicationConfiguration.class);

		startAceLogger(config);

		AceLogger
				.Instance()
				.log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
						"ApplicationServer.ApplicationServer() -- Ace Application server started");

		startRMI(config);

		startPluginAppConfiguration(config);

		startTimerService();

		initApps();

		new PolledAppServerAdapter();
	}

	private static void startTimerService() {
		AceTimer timer = new AceTimer();
		timer.start();
	}

	public void dispose() {
		instance = null;
	}

	public String getHostName() {
		return hostName;
	}

	public static void shutdown() {
		AceLogger
				.Instance()
				.log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
						"ApplicationServer.ShutdownThread() -- Shutting down the Application Server");

		try {
			unregisterMbeans();
		} catch (Exception e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ApplicationServer.ShutdownThread() -- Error shutting down MBean",
							e);
		}

		try {
			shutdownRMI();

			// delete all the applications
			int[] app_list = PluginAppList.Instance().listApplications();

			for (int i = 0; i < app_list.length; i++) {
				PluginAppList.Instance().delete(app_list[i]);
			}

			if (ApplicationServer.getInstance() != null) {
				ApplicationServer.getInstance().dispose();
			}

			if (AceTimer.Instance() != null) {
				AceTimer.Instance().dispose();
			}

			PolledAppServerAdapter.getInstance().dispose();

			System.out
					.println("Waiting for 15 seconds for threads to terminate gracefully...");
			Thread.sleep(15000L);

			if (AceLogger.Instance() != null) {
				AceLogger.Instance().dispose();
			}

			Thread.sleep(5000L);
			System.out.println("Ace Application Server has been shutdown");

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	private static void unregisterMbeans() throws MBeanRegistrationException,
			InstanceNotFoundException {
		for (ObjectName mbean : ApplicationServer.getInstance()
				.getRegisteredObjects().values()) {
			ApplicationServer.getInstance().getMbeanServer()
					.unregisterMBean(mbean);
		}
		ApplicationServer.getInstance().getRegisteredObjects().clear();
	}

	private static void shutdownRMI() {
		try {
			Naming.unbind(remoteServiceName);
			UnicastRemoteObject.unexportObject(aceRmi, true);
			Thread.sleep(1000L);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Registry rServer = ApplicationServer.getInstance().getRegistry();
			if (rServer != null) {

				for (String name : rServer.list()) {
					rServer.unbind(name);
				}

				UnicastRemoteObject.unexportObject(rServer, true);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MBeanServer getMbeanServer() {
		return mbeanServer;
	}

	private Map<String, ObjectName> getRegisteredObjects() {
		return registeredObjects;
	}

	public Object getBean(String beanName) {
		return context.getBean(beanName);
	}

	public <T> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	private static void startAceLogger(ApplicationConfiguration config)
			throws AceException {
		try {
			new AceLogger(config.getProcessName(), config.getLogGroup());
			AceLogger.Instance().start();
		} catch (Exception ex) {
			throw new AceException("System logger could not be started: "
					+ ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	private static void startRMI(ApplicationConfiguration config)
			throws AceException {
		try {
			if (config.isRegistry()) {
				// register the service
				ApplicationServer.getInstance()
						.setRegistry(
								LocateRegistry.createRegistry(config
										.getRegistryPort()));
				if (AceLogger.Instance() != null) {
					AceLogger
							.Instance()
							.log(AceLogger.INFORMATIONAL,
									AceLogger.SYSTEM_LOG,
									"HTTPApplicationConfiguration.processRemoteOperations() -- Registry service started at port "
											+ config.getRegistryPort());
				}
			}

			aceRmi = new AceRMIImpl();

			String url = null;
			if (config.getRegistryURL().endsWith("/")) {
				url = config.getRegistryURL();
			} else {
				url = config.getRegistryURL() + "/";
			}

			String host = InetAddress.getLocalHost().getHostName();

			remoteServiceName = url + config.getRegistryServiceName() + "/"
					+ host;
			Naming.rebind(remoteServiceName, aceRmi);

			if (AceLogger.Instance() != null) {
				AceLogger
						.Instance()
						.log(AceLogger.INFORMATIONAL,
								AceLogger.SYSTEM_LOG,
								"HTTPApplicationConfiguration.processRemoteOperations() -- HTTPSRemoteService bound with name "
										+ remoteServiceName);
			}
		} catch (Exception e) {
			throw new AceException(e.getClass().getName()
					+ " occured while loading RMI service: ", e);
		}
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	private static void startPluginAppConfiguration(
			ApplicationConfiguration config) throws AceException {
		for (PluginApplicationInfo plugin : config.getPlugins()) {
			PluginAppList.Instance().add(plugin);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}
}
