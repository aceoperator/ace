USE $$ACE(ACEOPERATOR_SQL_DB);

# Change on 10/18/2012
ALTER TABLE user_tbl 
	ADD COLUMN avatar VARCHAR(400) NULL  AFTER gatekeeper;
	
# Change on 10/22/2012
DROP TABLE IF EXISTS blacklist_tbl;

CREATE TABLE IF NOT EXISTS user_tbl_new (
  id bigint unsigned NOT NULL AUTO_INCREMENT,
  userid varchar(40) binary NOT NULL default '',
  password varchar(255) binary default '',
  domain varchar(40) default '',
  flags int default '0',
  fullname varchar(60) default '',
  address varchar(128) default '',
  addnl_info varchar(128) default '',
  unavail_xferto varchar(40) binary default '',
  gatekeeper varchar(40) default '',
  avatar varchar(400) NULL, 
  PRIMARY KEY (id),
  UNIQUE KEY (userid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO user_tbl_new (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, avatar)
    SELECT userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, avatar FROM user_tbl;
    
DROP TABLE user_tbl;

RENAME TABLE user_tbl_new TO user_tbl;

CREATE  TABLE blacklist_tbl (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `user_id` BIGINT UNSIGNED NOT NULL ,
  `cookie` VARCHAR(100) NOT NULL ,
  `level` TINYINT UNSIGNED NULL DEFAULT 0 ,
  `mod_time` TIMESTAMP NULL ,
   PRIMARY KEY (`id`),
   KEY (`user_id`),
   UNIQUE KEY (`user_id`, `cookie`),
   FOREIGN KEY (`user_id`) REFERENCES `user_tbl`(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
  
  
# Change on 11/09/2012  
RENAME TABLE user_security_questions_tbl TO user_security_questions_tbl_old;

CREATE TABLE IF NOT EXISTS user_security_questions_tbl (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  question_id TINYINT(4) NOT NULL ,
  question_value VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  answer_value VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  user_id BIGINT UNSIGNED NOT NULL ,
  PRIMARY KEY (id),
  UNIQUE KEY (question_id, user_id),
  FOREIGN KEY (user_id) REFERENCES user_tbl(id) ON DELETE CASCADE 
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;
  
 INSERT INTO  user_security_questions_tbl (question_id, question_value, answer_value, user_id) 
 SELECT q.questionid, q.question_value, q.answer_value, u.id
 FROM user_tbl u, user_security_questions_tbl_old q
 WHERE u.userid = q.userid;
 
 DROP TABLE user_security_questions_tbl_old;
 
 # Change on 11/29/2012
UPDATE group_tbl SET groupid = 'operator-group'
WHERE groupid = 'operator group';

UPDATE group_member_tbl SET groupid = 'operator-group'
WHERE groupid = 'operator group';

UPDATE group_owner_tbl SET groupid = 'operator-group'
WHERE groupid = 'operator group';

UPDATE canned_message_tbl SET grp = 'operator-group'
WHERE grp = 'operator group';

UPDATE opm_operator_tbl SET groupid = 'operator-group'
WHERE groupid = 'operator group';

DROP TABLE IF EXISTS ace_restricted_access_user_tbl;

# Change on 11/30/2012
ALTER TABLE user_tbl 
	ADD COLUMN locked TINYINT UNSIGNED DEFAULT '0' AFTER flags;
ALTER TABLE user_tbl 
	ADD COLUMN change_password TINYINT UNSIGNED DEFAULT '0' AFTER locked;

# Change on 12/02/2012
RENAME TABLE canned_message_tbl TO canned_message_tbl_bak;

CREATE TABLE canned_message_tbl
	(id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	grp VARCHAR(50) BINARY NULL,
	description VARCHAR(100) CHARACTER SET utf8 NOT NULL default '',
	message BLOB NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY (grp, description),
	CONSTRAINT `cannedmsgtbl_fk_1` FOREIGN KEY (grp) REFERENCES group_tbl (groupid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO canned_message_tbl (grp, description, message)
    SELECT grp, description, message from canned_message_tbl_bak;

DROP TABLE canned_message_tbl_bak;