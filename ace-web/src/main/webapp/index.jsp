<html>
<header>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">

<!-- Setup for mobile -->
<meta name="HandheldFriendly" content="true" />
<meta name="viewport"
	content="width=device-width; user-scalable=no; initial-scale=1.0; maximum-scale=1.0;" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="MobileOptimized" content="width" />
<title>Ace Contact Center Home Page</title>
</header>
<body>
	<script src="ao-browser-detect.js"></script>
	<script>

		function loadContactCenter() {
			
			var form = document.forms['contactcenter'];
			var profileName = form.elements['profile'].value;
			
			langSelection = form.elements['language'];
			var language = "";
			for (i = 0; i < langSelection.length; i++) {
				if (langSelection[i].selected == true) {
					language = langSelection[i].value;
					break;
				}
			}
			
			themeSelection = form.elements['theme'];
			var theme = "";
			for (i = 0; i < themeSelection.length; i++) {
				if (themeSelection[i].selected == true) {
					theme = themeSelection[i].value;
					break;
				}
			}
			
			var urlParamCount = 0;
			var dimension = "height=600,width=800"
			var url = 'Ace_web.html';
			var browser = detectBrowser();
			if (browser == 'mobile') {
				url = 'Ace_mobile.html'
			} else if (browser == 'tablet') {
				url = 'Ace_tablet.html'
			}

			if (profileName != null && profileName.length > 0) {
				if (urlParamCount == 0) {
					url = url + "?"
				} else {
					url = url + "&";
				}

				url = url + "profile=" + encodeURIComponent(profileName);
				urlParamCount++;
			}

			if (language != null && language.length > 0) {
				if (urlParamCount == 0) {
					url = url + "?"
				} else {
					url = url + "&";
				}

				url = url + "locale=" + encodeURIComponent(language);
				urlParamCount++;
			}

			if (theme != null && theme.length > 0) {
				if (urlParamCount == 0) {
					url = url + "?"
				} else {
					url = url + "&";
				}

				url = url + "theme=" + encodeURIComponent(theme);
				urlParamCount++;
			}

			if (browser == 'desktop') {
				window.open(url, '',
						'scrollbars=0,menubar=0,resizable=1,toolbar=0,location=0,status=0,'
								+ dimension);
			} else {
				window.location.href = url;
			}
			return false;
		}
	</script>

	<h3>Ace Contact Center</h3>
	Welcome to the Ace Contact Center.
	<p />

	<table style="width: 100%; text-align: left;">
		<tr style="background-color: black; color: white;">
			<th
				style="padding: 10px 10px 10px 10px; border-right: 2px solid black;">Ace
				Operator Links</th>
			<th style="padding: 10px 10px 10px 10px;">Contact Center Access</th>
		</tr>

		<tr style="vertical-align: top">
			<td
				style="width: 25%; border-right: 2px solid black; padding: 10px 10px 10px 10px;">
				<a href="http://www.quik-j.com" target="_blank">On the Web</a></br/> <a
				href="http://aceoperator.sourceforge.net/site/?p=215"
				target="_blank">License</a><br /> <a
				href="http://aceoperator.sourceforge.net/site/?page_id=245"
				target="_blank">Documentation</a><br /> <a href="/ace-communicator"
				target="_blank">Ace Communicator</a></td>

			<td style="width: 70%; padding: 10px 10px 10px 10px;">
				<form name="contactcenter" action="#" method="post">
					Profile<br /> <input type="text" name="profile">
					<p />

					Language<br /> <select name="language">
						<option value="" selected="selected">Select one</option>
						<option value="hr_HR">Croatian</option>
						<option value="nl_NL">Dutch</option>
						<option value="en_US">English</option>
					</select>
					<p />

					Theme<br /> <select name="theme">
						<option value="" selected="selected">Select one</option>
						<option value="chrome">Chrome</option>
						<option value="dark">Dark</option>
						<option value="chrome-small">Chrome Small</option>
						<option value="dark-small">Dark Small</option>
						<option value="chrome-large">Chrome Large</option>
						<option value="dark-large">Dark Large</option>
					</select>
					<p />

					<input name="operator" type="submit" value="Connect"
						onclick="return loadContactCenter();"> <input type="reset">
				</form></td>

		</tr>
	</table>

</body>
</html>