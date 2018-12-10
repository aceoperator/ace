USE $$ACE(ACEOPERATOR_SQL_DB);

-- Change on 03/10/2013

-- Adding foreign key constraints in the group_member_tbl. If the following alter table calls fail
-- because of foreign key constraints (i.e, there are rows in the child table that are not present
-- in parent table where we are adding the constraints), we need to clean out the child table and
-- re-run the alter statements. To clean out, we need a query similar to the one below
--
--		SELECT m.userid
--		FROM group_member_tbl m LEFT OUTER JOIN user_tbl u ON m.userid = u.userid
--		WHERE u.userid IS NULL;
--
-- The above query returns all the userid in the child table (group_member_tbl) that are not present
-- in the parent table. Next, delete the entries from the child table.


ALTER TABLE group_member_tbl
  ADD CONSTRAINT FK1_group_member_tbl
  FOREIGN KEY (userid)
  REFERENCES user_tbl (userid)
  ON DELETE CASCADE, 
  ADD CONSTRAINT FK2_group_member_tbl
  FOREIGN KEY (groupid)
  REFERENCES group_tbl (groupid)
  ON DELETE CASCADE,  
  ADD INDEX FK1_group_member_tbl (userid ASC),
  ADD INDEX FK2_group_member_tbl (groupid ASC);
  
ALTER TABLE group_owner_tbl
  ADD CONSTRAINT FK1_group_owner_tbl
  FOREIGN KEY (userid)
  REFERENCES user_tbl (userid)
  ON DELETE CASCADE, 
  ADD CONSTRAINT FK2_group_owner_tbl
  FOREIGN KEY (groupid)
  REFERENCES group_tbl (groupid)
  ON DELETE CASCADE,  
  ADD INDEX FK1_group_owner_tbl (userid ASC),
  ADD INDEX FK2_group_owner_tbl (groupid ASC);
  
-- Change on 3/13/2013
ALTER TABLE `group_member_tbl` 
	ADD UNIQUE INDEX `UK1_group_member_tbl` (`userid` ASC, `groupid` ASC);
	
-- Change on 3/15/2013
ALTER TABLE `feature_params_tbl` 
  ADD CONSTRAINT `FK1_feature_params_tbl`
  FOREIGN KEY (`feature_id` )
  REFERENCES `$$ACE(ACEOPERATOR_SQL_DB)`.`feature_tbl` (`id` )
  ON DELETE CASCADE
  ON UPDATE NO ACTION
, ADD INDEX `FK1_feature_params_tbl` (`feature_id` ASC) ;


-- Change on 8/5/2013
CREATE TABLE log_tbl LIKE ace.log_tbl;
INSERT INTO log_tbl SELECT * FROM ace.log_tbl;
DROP TABLE ace.log_tbl;

-- Change on 8/9/2013
CREATE TABLE account_tbl LIKE ace.account_tbl;
INSERT INTO account_tbl SELECT * FROM ace.account_tbl;
DROP TABLE ace.account_tbl;

ALTER TABLE `$$ACE(ACEOPERATOR_SQL_DB)`.`account_tbl` 
	DROP COLUMN `features` , 
	DROP COLUMN `level` , 
	DROP COLUMN `flags` , 
	DROP COLUMN `domain` , 
	CHANGE COLUMN `password` `password` VARCHAR(255)  NOT NULL;
	
-- Change on 8/10/2013 RUN THE FOLLOWING AS THE ROOT user
drop database ace;

-- Change on 09/08/2013
ALTER TABLE `opm_operator_tbl` CHANGE COLUMN `opm_value` `opm_value` FLOAT UNSIGNED NOT NULL DEFAULT '0.0';

DELETE FROM opm_operator_tbl WHERE opm_value = 0.0;

-- Change on 10/27/2013
ALTER TABLE `log_tbl` CHANGE COLUMN `message` `message` VARCHAR(2000) CHARACTER SET 'utf8' NOT NULL;
ALTER TABLE `user_tbl` ADD COLUMN `private` TINYINT NULL DEFAULT '0'  AFTER `avatar` ;

-- Change on 11/05/2013
ALTER TABLE `blacklist_tbl` 
	ADD COLUMN `type` TINYINT NOT NULL DEFAULT '0'  AFTER `identifier` , 
	CHANGE COLUMN `cookie` `identifier` VARCHAR(100) NOT NULL  
	, DROP INDEX `user_id_2` 
	, ADD UNIQUE INDEX `user_id_2` (`user_id` ASC, `identifier` ASC, `type` ASC) ;
	
-- Change on 05/29/2014
ALTER TABLE `user_tbl` ADD COLUMN `password_updated` DATETIME NULL  AFTER `private`;
UPDATE user_tbl SET password_updated = NOW();