/**
 * Created by trieu on 5/27/15.
 */
var modelUtils = require('../helpers/model_utils');

var express = require('express')
    , auth = require('../middlewares/auth')
    , router = express.Router();

router.use('/monitor', require('./monitor'))
router.use('/user', require('./user'))


router.get('/', auth, function(req, res) {
    res.redirect('/monitor/event');
})

router.get('/403', function (req, res) {
    var data = modelUtils.baseModel(req);
    res.render('common/no-auth', data);
});

module.exports = router;