USE $$ACE(ACEOPERATOR_SQL_DB);

-- Change on 3/8/2016
-- do it for all groups

insert into feature_params_tbl(feature_id, pname, pvalue) select id, 'display-wait-time', 'false' from feature_tbl;
