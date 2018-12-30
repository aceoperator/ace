
INSERT IGNORE INTO user_tbl (userid, password, domain, flags, fullname, address, addnl_info, unavail_xferto, gatekeeper, password_updated)
	VALUES (@user, password(@password),'ace', 4, @full_name, @email,'Operator','messagebox', '', NOW());
INSERT IGNORE INTO group_member_tbl values (@user,'operator-group');