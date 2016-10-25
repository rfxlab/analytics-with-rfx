/**
 * Created by trieu on 5/27/15.
 */

module.exports = function(req, res, next) {
    var sessionid = new Number(req.cookies.sessionid);
    //console.log('Time:', Date.now());
    //console.log("sessionid "+sessionid);
    req.sessionid = sessionid;
    next()
}