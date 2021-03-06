<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<html>
<body>
<script src="https://api-maps.yandex.ru/2.1/?lang=en_US" type="text/javascript"></script>
<script type="text/javascript">    ymaps.ready(function () {
    var myMap = new ymaps.Map('map_clusters', {
        center: [0, 0],
        zoom: 1,
        controls: ['zoomControl', 'typeSelector'],
        behaviors: ['default', 'scrollZoom']
    });
    clusterer = new ymaps.Clusterer({
        preset: 'islands#invertedVioletClusterIcons',
        groupByCoordinates: false,
        clusterDisableClickZoom: true,
        clusterHideIconOnBalloonOpen: false,
        geoObjectHideIconOnBalloonOpen: false
    }),
        getPointData = function (position) {
            return {
                balloonContentHeader: 'IP ' + (position.ip),
                balloonContentBody: getContentBody(position)

            }

        },
        getPointOptions = function () {
            return {
                preset: 'islands#violetIcon'
            };
        },
        points = ${points},
        geoObjects = [];

    function getContentBody(position) {
        var str = '';
        for (key in position) {
            if (position.hasOwnProperty(key)) {
                var value = position[key];
                str = str + key + ':' + value + '</br>'
            }
        }
        return str;
    }

    var jpositions =${jpositions};

    for (var i = 0, len = points.length; i < len; i++) {
        geoObjects[i] = new ymaps.Placemark(points[i],
            getPointData(jpositions[i]),
            getPointOptions());
    }
    clusterer.options.set({
        gridSize: 50,
        clusterDisableClickZoom: true
    });

    clusterer.add(geoObjects);
    myMap.geoObjects.add(clusterer);


    myMap.setBounds(clusterer.getBounds(), {
        checkZoomRange: true
    });
});
</script>

<div class="container" style="alignment: center">
    <%--<%@include file="header.jsp" %>--%>
    <br>
    <div id="map_clusters" style="width: 100%; height: 100%;"></div>
</div>
</body>
</html>


