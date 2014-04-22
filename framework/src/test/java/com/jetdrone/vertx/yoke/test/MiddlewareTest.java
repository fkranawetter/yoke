package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.Utils;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.vertx.testtools.VertxAssert.*;

public class MiddlewareTest extends TestVerticle {

    @Test
    public void testMiddleware() {
        final Yoke yoke = new Yoke(this);

        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertNotNull(this.vertx);
                testComplete();
            }
        });

        new YokeTester(vertx, yoke).request("GET", "/", null);
    }

    @Test
    public void testXml() throws TransformerException, XMLStreamException {
        String message = "\n" +
                "\n" +
                "<Customers>\n" +
                "    <Customer Id=\"99\">\n" +
                "        <Name>Bob</Name>\n" +
                "        <Age>39</Age>\n" +
                "        <Address>\n" +
                "            <Street>10 Idle Lane</Street>\n" +
                "            <City>Yucksville</City>\n" +
                "            <PostalCode>xxxyyy</PostalCode>\n" +
                "        </Address>\n" +
                "    </Customer>\n" +
                "    <Customer Id=\"101\">\n" +
                "        <Name>Bill</Name>\n" +
                "        <Age>39</Age>\n" +
                "        <LastName/>\n" +
                "        <Address>\n" +
                "            <Street>10 Idle Lane</Street>\n" +
                "            <City>Yucksville</City>\n" +
                "            <PostalCode>xxxyyy</PostalCode>\n" +
                "        </Address>\n" +
                "    </Customer>\n" +
                "\n" +
                "</Customers>\n" +
                "\n";

        JsonObject json = Utils.xmlToJson(message).getObject("Customers");
        assertNotNull(json);

        String xml = Utils.jsonToXml(json, "Customers");
        assertNotNull(xml);

        testComplete();
    }

//    @Test
//    public void testDateParser() throws ParseException {
//        String date = "2014-04-08T09:24:32.000 UTC";
//
//        SimpleDateFormat ISODATE;
//
//        ISODATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        ISODATE.parse(date);
//
//        ISODATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//        ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//
//        ISODATE.parse(date);
//    }
}
