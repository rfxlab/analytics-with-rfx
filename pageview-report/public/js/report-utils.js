/**
 * Created by trieu on 6/17/15.
 */

Date.prototype.yyyymmdd = function() {
    var yyyy = this.getFullYear().toString();
    var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
    var dd  = this.getDate().toString();
    return yyyy +'-'+ (mm[1]?mm:"0"+mm[0]) +'-'+ (dd[1]?dd:"0"+dd[0]); // padding
};

function formatNumber(number) {
    var number = number.toFixed(0) + '';
    var x = number.split('.');
    var x1 = x[0];
    var x2 = x.length > 1 ? '.' + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
}

function makeLinePlusBarChart(graphPlaceholderId, data) {
    data = data.map(function (series) {
        series.values = series.values.map(function (d) {
            return {x: d[0], y: d[1] }
        });
        return series;
    });
    var chart;
    var placeholder = '#' + graphPlaceholderId + ' svg';
    var margin = {top: 30, right: 50, bottom: 40, left: 50};

    chart = nv.models.linePlusBarChart()
        .margin(margin)
        .legendRightAxisHint(' (right axis)')
        .color(d3.scale.category10().range());

    chart.xAxis.tickFormat(function (d) {
        return d3.time.format('%d/%m/%Y-%Hh')(new Date(d))
    }).showMaxMin(false);

    chart.y1Axis.tickFormat(function (d) {
        return d3.format(',f')(d)
    });
    chart.y2Axis.tickFormat(function (d) {
        return d3.format(',f')(d)
    });
    chart.bars.forceY([0]).padData(false);

    chart.x2Axis.tickFormat(function (d) {
        return d3.time.format('%x')(new Date(d))
    }).showMaxMin(false);

    d3.select(placeholder)
        .datum(data)
        .transition().duration(500).call(chart);

    nv.utils.windowResize(chart.update);

    chart.dispatch.on('stateChange', function (e) {
        nv.log('New State:', JSON.stringify(e));
    });

    return chart;
}


function initJqueryDatePicker(){
    $(function () {
        var d = new Date();
        d.setDate(d.getDate() - 1);
        $("#from").datepicker({
            defaultDate: "+1w",
            changeMonth: true,
            numberOfMonths: 1,
            dateFormat: 'yy-mm-dd',
            onClose: function (selectedDate) {
                $("#to").datepicker("option", "minDate", selectedDate);
            }
        }).datepicker("setDate", d);

        $("#to").datepicker({
            defaultDate: "+1w",
            changeMonth: true,
            numberOfMonths: 1,
            dateFormat: 'yy-mm-dd',
            onClose: function (selectedDate) {
                $("#from").datepicker("option", "maxDate", selectedDate);
            }
        }).datepicker("setDate", new Date());
    });
}

function getDateFromValue(){
    return $('#from').val();
}

function getDateToValue(){
    return $('#to').val();
}