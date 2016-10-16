use webtalk;

INSERT INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('amit', password('a1b2c3d4'),'ace',0,'Amit Chatterjee','amit@localhost','Operator','messagebox', '', NOW());
INSERT INTO group_member_tbl values ('amit','operator-group');

INSERT INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES ('becky', password('a1b2c3d4'),'ace',0,'Becky McElroy','becky@localhost','Operator','messagebox', '', NOW());
INSERT INTO group_member_tbl values ('becky','operator-group');