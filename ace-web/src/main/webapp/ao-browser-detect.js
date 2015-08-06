/**
 * 
 */
function detectBrowser() {
	var browser = "desktop";
	var mobileBrowsers = [ "iPhone", "iPad", "Android", "MIDP", "Opera Mobi",
			"Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile",
			"Windows Phone", "HTC", "LG", "MOT", "Nokia", "Symbian", "Fennec",
			"Maemo", "Tear", "Midori", "armv", "Windows CE", "WindowsCE",
			"Smartphone", "240x320", "176x220", "320x320", "160x160", "webOS",
			"Palm", "Sagem", "Samsung", "SGH", "SonyEricsson", "MMP", "UCWEB" ];
	var userAgent = navigator.userAgent;
	for ( var i = 0; i < mobileBrowsers.length; i++) {
		var m = mobileBrowsers[i].toString();
		if (userAgent.indexOf(m) >= 0
				|| userAgent.indexOf(m.toUpperCase()) >= 0
				|| userAgent.indexOf(m.toLowerCase()) >= 0) {
			var area = window.innerWidth * window.innerHeight;
			if (area <= 600 * 400) {
				browser = "mobile";
			} else {
				browser = "tablet";
			}
			return browser;
		}
	}

	return browser;
}