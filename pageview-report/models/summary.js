/**
 * Created by trieu on 6/3/15.
 */

var _ = require('lodash-node/compat');
var moment = require('moment');

var redisClientUtils = require('../helpers/redis_client_utils');
var client = redisClientUtils.getRedisClientAdServer();
var oneWeek = 168;
var threeDays = 72;

function getData(timeStr) {
    client.hgetall('m:' + timeStr, function (err, object) {
        //console.log(object);
        console.log("\n ------------" + timeStr);
        _.forIn(object, function (value, key) {
            console.log(key + " : " + value);
        })
        var ctr = (object['v-c'] * 100) / object['v-i'];

        var ctrTrueView = (object['view100'] * 100) / object['creativeView'];
        console.log(ctr.toFixed(2) + " , " + ctrTrueView.toFixed(2));
    });
}

function processDataHourly(timeStr, done, collector, cb) {
    client.hgetall('m:' + timeStr, function (err, object) {
        var rs = {};
        var time = moment(timeStr, "YYYY-MM-DD-HH").toDate().getTime();

        if ((object instanceof Object)) {
            var click = parseInt(object['v-c'] == null ? 0 : object['v-c']);
            var imp = parseInt(object['v-i'] == null ? 0 : object['v-i']);
            var pv = parseInt(object['v-pv'] == null ? 0 : object['v-pv']);
            var view100 = parseInt(object['view100'] == null ? 0 : object['view100']);
            // var ctr = (((click * 100) / imp).toFixed(2));

            rs['view100'] = [time, view100];
            rs['click'] = [time, click];
            rs['pageview'] = [time, pv];
            rs['impression'] = [time, imp];
            collector(timeStr, rs, done, cb);
        } else {
            rs['view100'] = [time, 0];
            rs['click'] = [time, 0];
            rs['pageview'] = [time, 0];
            rs['impression'] = [time, 0];
            collector(timeStr, rs, done, cb);
        }
    });
}

function getSummaryImpressionAndPlayView(cb) {
    var currHour = (new Date()).getHours() + oneWeek;
    var impDataList = [];
    var pvDataList = [];

    while (currHour >= 0) {
        var hourStr = moment().subtract(currHour, "hours").format("YYYY-MM-DD-HH");
        currHour = currHour - 1;
        var done = false;
        if (currHour < 0) {
            done = true;
        }

        processDataHourly(hourStr, done, function (timeStr, rs, done, cb) {
            impDataList.push(rs['impression']);
            pvDataList.push(rs['pageview']);
            if (done) {
                cb(impDataList, pvDataList)
            }
            // console.log(timeStr + " " + JSON.stringify(rs));
        }, function (impDataList, pvDataList) {
            var sumImp = 0, avgImp = 0;
            for (var i in impDataList) {
                var imp = impDataList[i][1];
                sumImp += imp;
            }
            avgImp = Math.round(sumImp / impDataList.length);

            var sumPV = 0, avgPV = 0;
            for (var i in pvDataList) {
                sumPV += pvDataList[i][1];
            }
            avgPV = Math.round(sumPV / pvDataList.length);

            var impStats = {
                "key": "Impression",
                "color": "#FF8C00",
                "values": impDataList,
                "total": sumImp,
                "avg": avgImp
            };
            var pvStats = {
                "key": "PlayView",
                "bar": true,
                "color": "#337ab7",
                "values": pvDataList,
                "total": sumPV,
                "avg": avgPV
            };

            var data = [pvStats, impStats];
            //console.log(JSON.stringify(data));

            cb(data)
        });
    }
}


function getSummaryImpressionAndClick(cb) {
    var currHour = (new Date()).getHours() + threeDays;
    var impDataList = [];
    var clickDataList = [];

    while (currHour >= 0) {
        var hourStr = moment().subtract(currHour, "hours").format("YYYY-MM-DD-HH");
        currHour = currHour - 1;
        var done = false;
        if (currHour < 0) {
            done = true;
        }

        processDataHourly(hourStr, done, function (timeStr, rs, done, cb) {
            impDataList.push(rs['impression']);
            clickDataList.push(rs['click']);
            if (done) {
                cb(impDataList, clickDataList)
            }
            // console.log(timeStr + " " + JSON.stringify(rs));
        }, function (impDataList, clickDataList) {
            var sumImp = 0, avgImp = 0;
            for (var i in impDataList) {
                var imp = impDataList[i][1];
                sumImp += imp;
            }
            avgImp = Math.round(sumImp / impDataList.length)

            var sumClick = 0, avgClick = 0;
            for (var i in clickDataList) {
                var c = clickDataList[i][1];
                sumClick += c;
            }
            avgClick = Math.round(sumClick / clickDataList.length)

            var impStats = {
                "key": "Impression",
                "bar": true,
                "color": "#337ab7",
                "values": impDataList,
                "total": sumImp,
                "avg": avgImp
            };
            var clickStats = {
                "key": "Click",
                "color": "#FF8C00",
                "values": clickDataList,
                "total": sumClick,
                "avg": avgClick
            };

            var data = [impStats, clickStats ];
            //console.log(JSON.stringify(data));

            cb(data)
        });
    }
}

function getSummaryImpressionVsCompleteView(cb) {
    var currHour = (new Date()).getHours() + oneWeek ;
    var impDataList = [];
    var view100DataList = [];

    while (currHour >= 0) {
        var hourStr = moment().subtract(currHour, "hours").format("YYYY-MM-DD-HH");
        currHour--;
        var done = false;
        if (currHour == -1) {
            done = true;
        }

        processDataHourly(hourStr, done, function (timeStr, rs, done, cb) {
            impDataList.push(rs['impression']);
            view100DataList.push(rs['view100']);
            if (done) {
                cb(impDataList, view100DataList)
            }
            // console.log(timeStr + " " + JSON.stringify(rs));
        }, function (impDataList, view100DataList) {
            var sumImp = 0, avgImp = 0;
            for (var i in impDataList) {
                var imp = impDataList[i][1];
                sumImp += imp;
            }
            avgImp = Math.round(sumImp / impDataList.length)

            var sumView100 = 0, avgView100 = 0;
            for (var i in view100DataList) {
                var c = view100DataList[i][1];
                sumView100 += c;
            }
            avgView100 = Math.round(sumView100 / view100DataList.length)

            var impStats = {
                "key": "Impression",
                "bar": true,
                "color": "#337ab7",
                "values": impDataList,
                "total": sumImp,
                "avg":avgImp
            };
            var view100Stats = {
                "key": "Completed-AdView",
                "color": "#FF8C00",
                "values": view100DataList,
                "total": sumView100,
                "avg":avgView100
            };

            var data = [impStats, view100Stats];
            //console.log(JSON.stringify(data));

            cb(data)
        });
    }
}

function processPieChartData(prefix, timeStr, cb) {
    client.hgetall('m:' + timeStr, function (err, object) {
        var data = [];

        for(var k in object){
            if(k.indexOf(prefix) === 0){
                var label = k.replace(prefix,'');
                var v = parseInt(object[k],10);
                //console.log(k + ' ' + v);
                data.push({'label':label,value:v});
            }
        }
        if(cb instanceof Function){
            cb(data);
        }
    });
}

exports.getSummaryImpressionAndClick = function (cb) {
    getSummaryImpressionAndClick(cb);
}

exports.getSummaryImpressionAndPageView = function (cb) {
    getSummaryImpressionAndPlayView(cb);
}

exports.getSummaryImpressionVsCompleteView = function (cb) {
    getSummaryImpressionVsCompleteView(cb);
}

exports.processPieChartData = function (prefix, timeStr, cb) {
    processPieChartData(prefix, timeStr, cb);
}

//processPieChartData('view100-','2015-06-24',function(data){  console.log(data);  client.quit(); });

