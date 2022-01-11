# BAG2 to CSV

This project contains code for converting BAG2 XML to CSV.
The idea is to use the BAG2 XML to CSV conversion for deriving an address table for geocoding addresses.
In order to derive such an address table the CSV output of this tool will needs to be post processed.
That is outside the scope of this tool.

In one parse we want to:

- Convert BAG2 XML for objecttypes with a geometry to CSV
- Convert the geometry to a point
- Reproject the points to WGS84 (EPSG:4326) for use in web mapping

Apache Axiom is used for XML parsing:

- https://ws.apache.org/axiom/
- https://ws.apache.org/axiom/userguide/userguide.html

With Axiom it is possible to read the XML in a streaming fashion and build an object tree for parts of the XML on demand.
This gets us the performance of pull parsing together with the convenience of DOM parsing.

Converting an OMElement to a String using the toString() method will yield XML again.

```java
String xmlString = "<greetings><greeting>Hello World</greeting></greetings>";
OMElement xmlElement = AXIOMUtil.stringToOM(xmlString);
assertEquals(xmlString, xmlElement.toString());
```

TODO Use GeoTools for reading the geometry, converting it to a point, and reprojecting that point.

- https://docs.geotools.org/latest/javadocs/org/opengis/geometry/Geometry.html#getRepresentativePoint--

