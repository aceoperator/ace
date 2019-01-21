
INSERT IGNORE INTO patchlist (file_name, applied_on) VALUES (@file, NOW());
