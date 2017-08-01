var lastPlayTime;

function getTitle() {
    var title = document.getElementsByTagName("title")[0].innerText;
    return title.substring(0, title.length - 10);
}

function captureReady(v) {
/*	console.log("captureReady - A");
*/	if (isAd()) {
		console.log("captureReady - isAd");
		return;
	}
/*	console.log("captureReady - B");
*/    app.captureReady(getTitle());
}
function onProgress(v) {
	if (isAd()) {
		console.log("onProgress - isAd");
		return;
	}

    app.captureEnd(getTitle());
}

function onPlaying(v) {
	if (isAd()) {
		console.log("onPlaying - isAd");
		return;
	}

	app.onPlaying(getTitle(), v.duration);
}

function onPause(v) {
	if (isAd()) {
		console.log("onPause - isAd");
		return;
	}

	app.onPaused(getTitle(), v.duration);
}

function collapseList() {
	console.log("collapseList");

	try {
		if (document.getElementsByClassName("_mppb")[0].getElementsByTagName("ol").length > 0) {
			document.getElementsByClassName("_mttb")[0].click();
		}
	} catch (e) {
		console.error(e);
	}
}

function performAutoLike() {
/*	console.log("performAutoLike");
*/	if (app.isLoggedIn() == false) {
		return;
	}

	try {
		var like = Array.prototype.slice.call(document.getElementsByTagName('a')).filter(function(e){return e.title.indexOf("좋아") > 0})[0];
		if (like && like.getAttribute("aria-pressed") !== "true") {
			like.click();
			return;
		}
	} catch (e) {
		console.error(e);
	}
}

function isAd() {
	return document.getElementsByClassName("videoAdUi").length > 0;
}

var gv;
function init() {
    var v = gv = document.getElementsByTagName("video")[0];
    if (!v || v.tabIndex < 0) {
		/* console.log("F"); */
    	app.injectionFail();
    	return;
    }
    v.ontimeupdate = function () {
		var curr = v.currentTime;
		var duration = v.duration;

		app.onProgress(v.currentTime, v.duration);

		try {
/*			console.log( curr + " / " + duration + " - " + lastPlayTime);
*/			if (lastPlayTime < 1 && v.currentTime >= 1) {
/*				console.log("A");
*/				collapseList();
			} else if (lastPlayTime < 1.5 && v.currentTime >= 1.5) {
/*				console.log("B");
*/				performAutoLike();
			} else if (lastPlayTime < 3.5 && v.currentTime >= 3.5) {
/*				console.log("C");
*/				captureReady(v);
			} else {
/*				console.log("D");
*/			}
        } catch(err) {
        	console.error(err);
        }

        lastPlayTime = v.currentTime;
    };

	v.onplaying = function() {onPlaying(v);};
	v.onpause = function() {onPause(v);};
}
init();