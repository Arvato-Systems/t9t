//Override mimicMouseDown_
zk.Widget.mimicMouseDown_ = function (wgt, noFocusChange, which) {
    var modal = zk.currentModal;

    //Skip modal with noautofocus = true
    if (modal && modal.domExtraAttrs && modal.domExtraAttrs.noautofocus == 'true') return;

    if (modal && !wgt) {
        var cf = zk.currentFocus;
        if (cf && zUtl.isAncestor(modal, cf)) cf.focus(0);
        else modal.focus(0);
    } else if (!wgt || wgt.canActivate()) {
        if (!noFocusChange) {
            var wgtVParent;
            zk._prevFocus = zk.currentFocus;
            zk.currentFocus = wgt;
            if (wgt && (wgtVParent = wgt.$n('a')) && jq.nodeName(wgtVParent, 'button', 'input', 'textarea', 'a', 'select', 'iframe')) {

                var oldStyle;
                if (zk.ie) {
                    oldStyle = wgtVParent.style.position;
                    wgtVParent.style.position = 'fixed';
                }
                wgt.focus();
                if (zk.ie) wgtVParent.style.position = oldStyle;
            }
            zk._cfByMD = true;
            setTimeout(function () {
                zk._cfByMD = false;
                zk._prevFocus = null;
            }, 0);

        }

        if (wgt)
            zWatch.fire('onFloatUp', wgt, {triggerByClick: which});
        else
            for (var dtid in zk.Desktop.all)
                zWatch.fire('onFloatUp', zk.Desktop.all[dtid]);
    }
}

/**
 * Collapse the opened menu
 * @returns
 */
function collapseHeaderMenu() {
    let e = document.getElementsByClassName("z-nav-open");
    if (e.length > 0) {
        setTimeout(function () {
            e[0].children[0].click();
        }, 100);
    }
}

function collapseHeaderMenuWithDelay(delay) {
    if (delay === undefined) {
        delay = 1000;
    }
    setTimeout(function () {
        collapseHeaderMenu()
    }, delay);
}

/** Session Timeout BEGIN **/

var isAlertShowing = false;
keepSessionAlive();
setInterval(function () {
    let maxTimeout = 600000;
    let alertBefore = 120000;
    let lastActive = window.localStorage.getItem('app.lastActive');

    if (!isAlertShowing && zAu.sentTime !== undefined && lastActive < zAu.sentTime) {
        window.localStorage.setItem('app.lastActive', zAu.sentTime);
        lastActive = zAu.sentTime;
    }

    let idle = Date.now() - lastActive;
    if (idle > maxTimeout) {
        //console.log('Session expired. Logging out');
        zAu.send(new zk.Event(zk.Widget.$('$mainHome'), "onLogout", {'': {'data': {'nodeId': ''}}}, {toServer: true}));
    } else if (idle > (maxTimeout - alertBefore)) {
        if (!isAlertShowing) {
            isAlertShowing = true;
            zAu.send(new zk.Event(zk.Widget.$('$mainHome'), "onSessionExpirationAlert", {'': {'data': {'nodeId': ''}}}, {toServer: true}));
        }
    }

}, 2000);

function setAppCurrentPageTitle(title) {
    let e = document.getElementsByClassName("app-cur-page-title")[0];
    if (e != undefined) {
        e.textContent = title;
    }
}

function keepSessionAlive() {
    isAlertShowing = false;
    window.localStorage.setItem('app.lastActive', Date.now());
}

/** Session Timeout END **/

/**
 * Collapse opened menu when click outside the navbar / Default Screen menu
 */
$(document).ready(function () {
    $(document).click(function (event) {
        let opened = document.getElementsByClassName("z-nav-open").length > 0;
        let _target = $(event.target);
        if (opened == true && !(_target.hasClass('z-menuitem-text') || _target.hasClass('z-nav-text') || _target.hasClass('z-nav-content') || _target.hasClass('header-nav-subtitle'))) {
            setTimeout(function () {
                var e = document.getElementsByClassName("z-nav-open")[0];
                if (e != undefined) {
                    e.children[0].click();
                }
            }, 100);
        }
    });

    //ZK navbar will automatically pop the header menu at the page load (Default Screen),
    //to fix that we need to detect and collapse the menu on pageload.
    setTimeout(function () {
        collapseHeaderMenu()
    }, 1000);
});

