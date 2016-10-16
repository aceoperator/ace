docker pull centos


docker run -it \
  --security-opt seccomp=unconfined \
  --cap-add=SYS_ADMIN \
  -e "container=docker" \
  -v /sys/fs/cgroup:/sys/fs/cgroup \
  centos:7 /usr/sbin/init