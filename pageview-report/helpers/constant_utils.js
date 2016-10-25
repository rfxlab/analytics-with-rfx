/**
 * Created by trieu on 5/27/15.
 */

var userNames = {1000:'Admin', 1001: 'Manager', 1002: 'Customer'};


exports.getUserName = function(id) {
    return userNames[id];
};

