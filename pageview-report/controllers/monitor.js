/**
 * Created by trieu on 5/27/15.
 */

var express = require('express')
    , router = express.Router()
    , auth = require('../middlewares/auth')
    , modelUtils = require('../helpers/model_utils')
    , constantUtils = require('../helpers/constant_utils')
    , Summary = require('../models/summary')
    , request = require('request')
    , moment = require('moment');


function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

router.get('/geolocation', auth, function (req, res) {
    var data = modelUtils.baseModel(req);
    data.dashboard_title = "Geo-Location";
    res.render('monitor/geo-heatmap', data)
});

router.get('/event', auth, function (req, res) {
    var data = modelUtils.baseModel(req);

    data.dashboard_title = "Super Web Analytics";
    data.sites = [];
    data.sites.push({value: "demo-pv", label: "PageView of Landing Page" });

    request(data.site.api_domain + '/api/sites/tgrm2016/sum', function (error, response, body) {
        if (!error && response.statusCode == 200) {
            var json = JSON.parse(body);
            data.pv = numberWithCommas(json.pv);
            data.imp = numberWithCommas(json.imp);
            data.impNgoiSao = numberWithCommas(json.impNgoiSao);
            data.impVideoAd = numberWithCommas(json.impVideoAd);
            data.plvLive = numberWithCommas(json.plvLive);
            data.reach = numberWithCommas(json.reach);
        }
        res.render('monitor/event', data)
    });

});

router.get('/summary/:graphId', function (req, res) {
    var cb = function (data) {
        //console.log(data.length)
        res.json(data);
    }
    var filterDate = req.query.filterDate != null ? req.query.filterDate : '2015-07-30';

    if(req.params.graphId === 'chartImpVsPv'){
        Summary.getSummaryImpressionAndPageView(cb)
    }
    else if(req.params.graphId === 'chartImpVsView100'){
        Summary.getSummaryImpressionVsCompleteView(cb)
    }
    else if(req.params.graphId === 'chartImpVsClick'){
        Summary.getSummaryImpressionAndClick(cb)
    }
    else if(req.params.graphId === 'chartPieOS'){
        Summary.processPieChartData('os-', filterDate, cb)
    }
    else if(req.params.graphId === 'chartPiePlatform'){
        Summary.processPieChartData('pf-', filterDate, cb)
    }
    else {
        Summary.getSummaryImpressionAndClick(cb)
    }
});

module.exports = router;