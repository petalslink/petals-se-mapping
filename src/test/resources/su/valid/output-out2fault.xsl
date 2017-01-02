<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2016-2017 Linagora
 
 This program/library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 2.1 of the License, or (at your
 option) any later version.
 
 This program/library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program/library; If not, see http://www.gnu.org/licenses/
 for the GNU Lesser General Public License version 2.1.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mapping-xsl="http://petals.ow2.org/se/mapping/xsl/param/output/1.0"
   xmlns:facture="http://petals.ow2.org/se/mapping/unit-test/facture" xmlns:ged="http://petals.ow2.org/se/mapping/unit-test/ged">

   <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" />

   <xsl:param name="mapping-xsl:incoming-request" />

   <xsl:template match="/">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="ged:supprimerResponse">
      <xsl:element name="factureInconnue" namespace="http://petals.ow2.org/se/mapping/unit-test/facture">
         <xsl:element name="identifiant" namespace="http://petals.ow2.org/se/mapping/unit-test/facture">
            <xsl:value-of select="$mapping-xsl:incoming-request/facture:supprimer/facture:identifiant" />
         </xsl:element>
      </xsl:element>
   </xsl:template>


</xsl:stylesheet>
