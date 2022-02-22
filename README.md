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

GeoTools is used for reading the geometry, converting it to a point if necessary, and reprojecting that point.
GeoTools in turn relies on JTS Topology Suite for the implementation of the geometry data structure and provides some help for working with JTS Topology Suite.

In order to convert non-point geometries to a point there are at least three options ( see http://lin-ear-th-inking.blogspot.com/2020/04/maximum-inscribed-circle-and-largest.html ):

- use getCentroid ( https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html )
- use getInteriorPoint ( https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html )
- use the center of the Maximum Inscribed Circle a.k.a. the Pole of Inaccesability ( https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/construct/MaximumInscribedCircle.html , see for usage example https://github.com/geotools/geotools/blob/main/modules/unsupported/process-geometry/src/main/java/org/geotools/process/geometry/PolyLabeller.java )

For now use getInteriorPoint. 
The centroid may be outside of the geometry, which is undesirable.
The Pole of Inaccessability is typically used for labeling geometries.
Should we have a flag to include the Pole of Inaccessability?

TODO Silence hsqldb logging, see also https://stackoverflow.com/questions/9931156/is-there-a-way-to-silence-hsqldb-logging

## BAG2 resources

- https://imbag.github.io/catalogus/hoofdstukken/conceptueelmodel
- https://imbag.github.io/praktijkhandleiding/objecttypen

Do we need to do something with the history (voorkomens)?
Or does it suffice to look at `status` to see if an object is still present at a certain reference date.

See https://imbag.github.io/catalogus/hoofdstukken/domeinwaarden for values that `status` may take (StatusPlaats, StatusVerblijfsobject).

See https://imbag.github.io/praktijkhandleiding/artikelen/wat-is-het-verschil-tussen-actieve-voorkomens-actuele-voorkomens-en-huidige-voorkomens about voorkomens.

> Het huidige voorkomen is een actueel voorkomen dat een status heeft dat aangeeft dat het object niet in historie geplaatst is. In historie geplaatste statussen (eindstatussen) zijn: naamgeving ingetrokken, woonplaats ingetrokken, plaats ingetrokken, niet gerealiseerd verblijfsobject, verblijfsobject ingetrokken, verblijfsobject ten onrechte opgevoerd, niet gerealiseerd pand, pand gesloopt en pand ten onrechte opgevoerd.
> Met andere woorden: een huidig voorkomen is een actueel voorkomen zonder eindstatus.

According to the `IMBAGLV_Objecten` XSD there is 1 voorkomen per Object stand. Where voorkomen occurs both minOccurs and maxOccurs are 1.

````xml
<xs:element name="voorkomen" minOccurs="1" maxOccurs="1">
	<xs:complexType>
		<xs:sequence>
			<xs:element ref="Historie:Voorkomen" minOccurs="1" maxOccurs="1"/>
		</xs:sequence>
	</xs:complexType>
</xs:element>
````

A voorkomen may or may not have an eindGeldigheid.  

````xml
<xs:element name="Voorkomen" type="Historie:Voorkomen" abstract="false"/>
<xs:complexType name="Voorkomen" abstract="false">
	<xs:sequence>
		<xs:element name="voorkomenidentificatie" type="xs:integer" minOccurs="1" maxOccurs="1"/>
		<xs:element name="beginGeldigheid" type="xs:date" minOccurs="1" maxOccurs="1"/>
		<xs:element name="eindGeldigheid" type="xs:date" minOccurs="0" maxOccurs="1"/>
		<xs:element name="tijdstipRegistratie" type="xs:dateTime" minOccurs="1" maxOccurs="1"/>
		<xs:element name="eindRegistratie" type="xs:dateTime" minOccurs="0" maxOccurs="1"/>
		<xs:element name="tijdstipInactief" type="xs:dateTime" minOccurs="0" maxOccurs="1"/>
		<xs:choice minOccurs="1" maxOccurs="1">
			<xs:element ref="Historie:BeschikbaarLV"/>
		</xs:choice>
	</xs:sequence>
</xs:complexType>
````

If there are multiple entries in the XML for the same object then the status gives the status for that object at that time. The entry with the latest documentdatum is the latest entry. Earlier entries should have an eindGeldigheid. Whether the object is in history or not will be indicated by the status of the latest entry. For example, object 0009030000000001 is een `huidig voorkomen` as it has eindGeldigheid is empty and its status is not an end status. Object 0010030000100100 is an `actueel voorkomen`, but not a `huidig voorkomen` as its status is an end status. 

```
identificatie, voorkomenidentificatie, beginGeldigheid, eindGeldigheid, status, documentdatum
0009030000000001, 1, 2017-07-25, 2020-03-03, Plaats aangewezen, 2017-07-25
0009030000000001, 2, 2020-03-03, , Plaats aangewezen, 2020-03-03
0010030000100100, 1, 2010-07-13, 2013-07-09, Plaats aangewezen, 2010-07-13
0010030000100100, 2, 2013-07-09, , Plaats ingetrokken, 2013-07-09
```

## XPath with Axiom

````java
// Extraction via Xpath seems overhead
// Below code how to
AXIOMXPath xPathNr = new AXIOMXPath("Objecten:heeftAlsHoofdadres/Objecten-ref:NummeraanduidingRef");
xPathNr.addNamespace(NAMESPACE_PREFIX_OBJECTEN, NAMESPACE_URI_OBJECTEN);
xPathNr.addNamespace(NAMESPACE_PREFIX_OBJECTEN_REF, NAMESPACE_URI_OBJECTEN_REF);
OMElement nummeraanduiding = (OMElement) xPathNr.selectSingleNode(element);
if(nummeraanduiding != null) {
    System.out.println("HoofdAdresNummeraanduiding: " + nummeraanduiding.getText());
    record.put("HoofdadresNummeraanduiding", nummeraanduiding.getText());
}

AXIOMXPath xPathId = new AXIOMXPath("./Objecten:identificatie");
xPathId.addNamespace(NAMESPACE_PREFIX_OBJECTEN, NAMESPACE_URI_OBJECTEN);
OMElement identificatie = (OMElement) xPathId.selectSingleNode(element);
if(identificatie != null) {
    System.out.println("Identificatie: " + identificatie.getText());
    record.put("Identificatie", identificatie.getText());
}
````