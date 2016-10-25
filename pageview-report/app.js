
var express = require('express');
var app = express();
var expressHbs = require('express-handlebars');
var favicon = require('serve-favicon');

var morgan = require('morgan');             // log requests to the console (express4)
var bodyParser = require('body-parser');    // pull information from HTML POST (express4)
var cookieParser = require('cookie-parser');// pull information from HTML cookies (express4)

var siteConfigs = require('./configs/site.js');
var dbConfig = require('./configs/database');

//console.log(siteConfigs);
//console.log(dbConfig.url);
app.use(favicon(__dirname + '/public/css/images/favicon.ico'));
app.use(require('express-promise')());
app.use(express.static(__dirname + '/public'));
app.use(bodyParser.json()); // for parsing application/json
app.use(bodyParser.urlencoded({ extended: true })); // for parsing application/x-www-form-urlencoded
app.use(cookieParser());
app.set('trust proxy', ['loopback', 'linklocal', 'uniquelocal']);

//app.use(morgan('dev'));       // log every request to the console

//TODO
app.listen(8282);

//config view engine
var hbsConfigs = {extname: 'hbs', defaultLayout: 'admin.hbs'};
app.engine('hbs', expressHbs(hbsConfigs));
app.set('view engine', 'hbs');
//TODO enable cache for production only
//app.enable('view cache');


var users = require('./middlewares/users');
app.use(users);

app.use(require('./controllers/index')); //load all controllers