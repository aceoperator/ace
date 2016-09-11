
use mysql;

GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@localhost IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';
GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@localhost.localdomain IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';
GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@'%' IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';

CREATE DATABASE webtalk;
USE webtalk;

CREATE TABLE `account_tbl` (
  userid VARCHAR(40) NOT NULL,
  password VARCHAR(255) NOT NULL,
  addnl_info VARCHAR(128) DEFAULT '',
  PRIMARY KEY (userid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO account_tbl (userid, password, addnl_info) VALUES ('ace', PASSWORD('a1b2c3d4'), 'Ace Super User');

CREATE TABLE  log_tbl
   (dated   TIMESTAMP NOT NULL,
    level   VARCHAR(10) NOT NULL,
    message VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    process VARCHAR(255) NOT NULL,
    KEY (dated),
    KEY (level),
    KEY (process)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  group_tbl (
  groupid varchar(64) binary NOT NULL default '',
  domain varchar(40) default '',
  flags int default '0',
  memberlogin_notif tinyint(4) default '1',
  memberbusy_notif tinyint(4) default '0',
  ownerlogin_notif tinyint(4) default '0',
  ownerbusy_notif tinyint(4) default '0',
  PRIMARY KEY (groupid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  user_tbl (
  id bigint unsigned NOT NULL AUTO_INCREMENT,
  userid varchar(40) binary NOT NULL default '',
  password varchar(255) binary default '',
  domain varchar(40) default '',
  flags int default '0',
  locked tinyint unsigned default '0',
  change_password tinyint unsigned default '0',
  fullname varchar(60) default '',
  address varchar(128) default '',
  addnl_info varchar(128) default '',
  unavail_xferto varchar(40) binary default '',
  gatekeeper varchar(40) default '',
  avatar varchar(400) NULL,
  private TINYINT NULL DEFAULT '0',
  password_updated DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (userid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE group_owner_tbl (
  `userid` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `groupid` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`groupid`),
  KEY `userid` (`userid`),
  KEY `FK1_group_owner_tbl` (`userid`),
  KEY `FK2_group_owner_tbl` (`groupid`),
  CONSTRAINT `FK1_group_owner_tbl` FOREIGN KEY (`userid`) REFERENCES `user_tbl` (`userid`) ON DELETE CASCADE,
  CONSTRAINT `FK2_group_owner_tbl` FOREIGN KEY (`groupid`) REFERENCES `group_tbl` (`groupid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE group_member_tbl (
  `userid` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `groupid` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  UNIQUE KEY `UK1_group_member_tbl` (`userid`,`groupid`),
  KEY `userid` (`userid`),
  KEY `groupid` (`groupid`),
  KEY `FK1_group_member_tbl` (`userid`),
  KEY `FK2_group_member_tbl` (`groupid`),
  CONSTRAINT `FK1_group_member_tbl` FOREIGN KEY (`userid`) REFERENCES `user_tbl` (`userid`) ON DELETE CASCADE,
  CONSTRAINT `FK2_group_member_tbl` FOREIGN KEY (`groupid`) REFERENCES `group_tbl` (`groupid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `blacklist_tbl` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `type` tinyint(4) NOT NULL DEFAULT '0',
  `level` tinyint(3) unsigned DEFAULT '0',
  `mod_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_2` (`user_id`,`identifier`,`type`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `blacklist_tbl_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_tbl` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;


INSERT INTO group_tbl VALUES ('operator-group','ace',0,3,3,0,0);
INSERT INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('operator',password('a1b2c3d4'),'ace',0,'Ace Operator group owner','','Owner of operator group','messagebox', '', NOW());
INSERT INTO group_owner_tbl values ('operator','operator-group');
INSERT INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated) 
	VALUES ('messagebox',password('a1b2c3d4'),'ace',0,'Message service feature','','Message service feature - handles offline messaging','', '', NOW());

CREATE TABLE  cdr_logout_tbl (
  loginid varchar(255) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  index loginid (loginid(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_reg_login_tbl (
  loginid varchar(255) binary NOT NULL default '',
  username varchar(40) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  PRIMARY KEY (loginid),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_disc_tbl (
  session varchar(255) binary NOT NULL default '',
  code int NOT NULL default 0,
  time_stamp datetime NOT NULL default 0,
  index session (session(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_join_tbl (
  first_session varchar(255) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  more_sessions blob,
  index first_session (first_session(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_leave_tbl (
  session varchar(255) binary NOT NULL default '',
  endpoint varchar(255) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  index session (session(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_setup_tbl (
  session varchar(255) binary NOT NULL default '',
  calling varchar(255) binary NOT NULL default '',
  called varchar(255) binary NOT NULL default '',
  transferid varchar(255) binary default '',
  time_stamp datetime NOT NULL default 0,
  index session (session(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_setup_resp_tbl (
  session varchar(255) binary NOT NULL default '',
  loginid varchar(255) binary NOT NULL default '',
  status smallint NOT NULL default 0,
  time_stamp datetime NOT NULL default 0,
  index session (session(100)),
  index timestamp (time_stamp),
  index status (status)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_session_transfer_tbl (
  session varchar(255) binary NOT NULL default '',
  transferto varchar(255) binary NOT NULL default '',
  identifier varchar(255) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  index session (session(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  cdr_unreg_login_tbl (
  loginid varchar(255) binary NOT NULL default '',
  name varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci default '',
  address varchar(255) binary default '',
  time_stamp datetime NOT NULL default 0,
  addnl_info varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci default '',
  ip_address varchar(45),
  cookie varchar(100),
  index loginid (loginid(100)),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- TODO change the column name groupid to group or owner (the column contains a user name
-- who is an owner of a group
CREATE TABLE  cdr_unreg_opbusy_tbl (
  groupid varchar(64) binary NOT NULL,
  time_stamp datetime NOT NULL default 0,
  index groupid (groupid),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  opm_operator_tbl (
  groupid varchar(64) binary NOT NULL default '',
  time_stamp datetime NOT NULL default 0,
  opm_name varchar(64) binary NOT NULL default '',
  opm_value float unsigned not null default '0.0',
  KEY (groupid),
  KEY (time_stamp),
  KEY (opm_name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE canned_message_tbl
	(id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	grp VARCHAR(50) BINARY NULL,
	description VARCHAR(100) CHARACTER SET utf8 NOT NULL default '',
	message BLOB NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY (grp, description),
	CONSTRAINT `cannedmsgtbl_fk_1` FOREIGN KEY (grp) REFERENCES group_tbl (groupid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into canned_message_tbl (grp, description, message)
	values ('operator-group', 'QUIK Computing greetings message',
		'Welcome to QUIK Computing operator services demo. How may I help you?');

insert into canned_message_tbl (grp, description, message)
	values ('operator-group', 'QUIK Computing web page',
		'<a href=http://www.quik-computing.com target=blank_>Click here to visit our web site</href>');

insert into canned_message_tbl (grp, description, message)
	values ('operator-group', 'QUIK Computing e-mail feedback',
		'<a href=mailto:info@quik-j.com?subject=Feedback target=_blank>Click here to send us a feedback by email</a>');

CREATE TABLE  feature_tbl (
  id int unsigned NOT NULL AUTO_INCREMENT,
  fname varchar(40) binary NOT NULL,
  domain varchar(40) default '',
  class varchar(255) binary NOT NULL,
  active tinyint default 0,
  PRIMARY KEY (id),
  UNIQUE (fname)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `feature_params_tbl` (
  `feature_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pname` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `pvalue` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`feature_id`,`pname`),
  KEY `FK1_feature_params_tbl` (`feature_id`),
  CONSTRAINT `FK1_feature_params_tbl` FOREIGN KEY (`feature_id`) REFERENCES `feature_tbl` (`id`) 
  	ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into feature_tbl values (NULL, 'operator', 'ace', 'com.quikj.application.web.talk.feature.operator.Operator', 1);
insert into feature_params_tbl values (LAST_INSERT_ID(), 'max-sessions', '1');
insert into feature_params_tbl values (LAST_INSERT_ID(), 'max-operators', '10');
insert into feature_params_tbl values (LAST_INSERT_ID(), 'max-queue-size', '20');

insert into feature_tbl values (NULL, 'messagebox', 'ace', 'com.quikj.application.web.talk.feature.messagebox.server.MessageBox', 1);
insert into feature_params_tbl values (LAST_INSERT_ID(), 'from', 'messagebox@mydomain.com');

CREATE TABLE  user_security_questions_tbl (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  question_id TINYINT(4) NOT NULL ,
  question_value VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  answer_value VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  user_id BIGINT UNSIGNED NOT NULL ,
  PRIMARY KEY (id),
  UNIQUE KEY (question_id, user_id),
  FOREIGN KEY (user_id) REFERENCES user_tbl(id) ON DELETE CASCADE 
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;
