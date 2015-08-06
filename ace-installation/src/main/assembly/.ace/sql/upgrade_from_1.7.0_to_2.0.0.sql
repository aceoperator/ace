
-- Manual Steps
-- (1) Convert all the MyIsam tables to InnoDb tables
-- (2) In the canned_msg_tbl, manually modify all the message columns (blob). 
--     Remove <text></text> from text elements. 
--     Replace <href></href> with actual HTML syntax (use <a> the target attribute).

-- Run the following SQL

use webtalk;

DELETE FROM feature_params_tbl WHERE pname = 'proactive-monitoring';

DROP TABLE ace_restricted_access_user_tbl;

delete from canned_message_tbl where message like '%<xml-form-req>%';

use ace;

DROP TABLE log_tbl;

CREATE TABLE IF NOT EXISTS log_tbl
   (dated   TIMESTAMP NOT NULL,
    level   VARCHAR(10) NOT NULL,
    message VARCHAR(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    process VARCHAR(255) NOT NULL,
    KEY (dated),
    KEY (level),
    KEY (process)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

use webtalk;
CREATE TABLE IF NOT EXISTS cdr_unreg_opbusy_tbl (
  groupid varchar(64) binary NOT NULL,
  time_stamp datetime NOT NULL default 0,
  index groupid (groupid),
  index timestamp (time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE cdr_unreg_login_tbl 
	ADD COLUMN ip_address VARCHAR(45) NULL  AFTER addnl_info, 
	ADD COLUMN cookie VARCHAR(100) NULL  AFTER ip_address;
	
ALTER TABLE cdr_unreg_login_tbl 
	modify name varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci default '';
	
use webtalk;
CREATE TABLE IF NOT EXISTS user_security_questions_tbl (
  questionid TINYINT(4) NOT NULL ,
  question_value VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  answer_value VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  userid VARCHAR(40) NOT NULL ,
  PRIMARY KEY (`questionid`, `userid`) 
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;
