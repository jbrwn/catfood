<#-- @ftlvariable name="model" type="dog.catfood.controllers.GetDeviceResponse" -->
<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#setting number_format="0.######">
<#import "../_dashboardLayout.ftl" as layout />
<#import "../_layout.ftl" as base />
<@layout.layout>
    <nav aria-label="breadcrumb">
        <ul>
            <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
            <li><a href="${routes.href("Devices", "details", { 'deviceId': model.device.id })}">${model.device.name}</a></li>
            <li>Live</li>
        </ul>
    </nav>
    <#if model.location??>
        <article>
            <header>
                <nav>
                    <ul>
                        <li>
                            <strong>Device Location</strong><br/>
                            <small>Last Ping: <span id="ping">${model.location.recordedOn.format()}</span></small>
                        </li>
                    </ul>
                </nav>
            </header>
            <div id="map"></div>
        </article>
        <script src='https://api.mapbox.com/mapbox-gl-js/v2.13.0/mapbox-gl.js'></script>
        <link href='https://api.mapbox.com/mapbox-gl-js/v2.13.0/mapbox-gl.css' rel='stylesheet' />
        <script>
            mapboxgl.accessToken = '${base.mapboxKey}';
            const map = new mapboxgl.Map({
                container: 'map', // container ID
                style: 'mapbox://styles/mapbox/streets-v12', // style URL
                center: [${model.location.longitude}, ${model.location.latitude}], // starting position [lng, lat]
                zoom: 15, // starting zoom
            });
            const marker = new mapboxgl.Marker()
                .setLngLat([${model.location.longitude}, ${model.location.latitude}])
                .addTo(map);
            const refreshRate = 2000;
            let lastLocationId=${model.location.id}
            const poll = () => {
                fetch('/_api/devices/${model.device.id}/Locations?limit=1')
                    .then((response) => response.json())
                    .then((data) => {
                        if (data && data[0].id !== lastLocationId) {
                            lastLocationId = data[0].id
                            document.getElementById("ping")
                                .firstChild
                                .textContent = new Intl.DateTimeFormat('en-US', { dateStyle: 'medium', timeStyle: 'medium'}).format(new Date(data[0].created_on))
                            marker.setLngLat([data[0].longitude, data[0].latitude])
                            map.flyTo({
                                center: [data[0].longitude, data[0].latitude],
                                essential: true
                            });
                        }
                    });
                setTimeout(poll, refreshRate)
            };
            poll();
        </script>
    </#if>
</@layout.layout>
