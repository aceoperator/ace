use $$ACE(ACEOPERATOR_SQL_DB);

INSERT IGNORE INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('amit', password('a1b2c3d4'),'ace', 4,'Amit Chatterjee','amit@acedemo.net','Operator','messagebox', '', NOW());
INSERT IGNORE INTO group_member_tbl values ('amit','operator-group');

INSERT IGNORE INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('becky', password('a1b2c3d4'),'ace', 4,'Becky McElroy','becky@acedmo.net','Operator','messagebox', '', NOW());
INSERT IGNORE INTO group_member_tbl values ('becky','operator-group');

INSERT IGNORE INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('operations', password('a1b2c3d4'),'ace', 7,'Operations Manager','operations@acedmo.net','Admin','messagebox', '', NOW());
INSERT IGNORE INTO group_member_tbl values ('operations','operator-group');