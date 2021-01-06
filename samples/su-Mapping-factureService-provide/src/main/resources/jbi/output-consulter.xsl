<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2016-2021 Linagora
 
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:mapping-xsl-properties="http://petals.ow2.org/se/mapping/xsl/param/1.0" xmlns:ged="http://service.server.ged.mapping.samples.petals.ow2.org/"
   xmlns:xop="http://www.w3.org/2004/08/xop/include" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

   <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no" />

   <xsl:template match="/">
      <xsl:apply-templates select="ged:consulterResponse | soapenv:Fault"/>
   </xsl:template>

   <xsl:template match="ged:consulterResponse">
      <xsl:element name="consulterResponse" namespace="http://facture.mapping.samples.petals.ow2.org/">
         <xsl:apply-templates select="file" />
      </xsl:element>
   </xsl:template>

   <xsl:template match="soapenv:Fault">
      <xsl:apply-templates select="detail"/>
   </xsl:template>
   
   <xsl:template match="detail">
      <xsl:apply-templates select="ged:documentInconnu"/>
   </xsl:template>
   
   <xsl:template match="ged:documentInconnu">
      <xsl:element name="factureInconnue" namespace="http://facture.mapping.samples.petals.ow2.org/">
         <xsl:element name="identifiant">
            <xsl:value-of select="reference" />
         </xsl:element>
      </xsl:element>
   </xsl:template>

   <xsl:template match="file">
      <xsl:element name="file">
         <xsl:apply-templates select="xop:Include" />
      </xsl:element>
   </xsl:template>

   <xsl:template match="xop:Include">
      <xsl:element name="Include" namespace="http://www.w3.org/2004/08/xop/include">
         <xsl:attribute name="href"><xsl:value-of select="@href" /></xsl:attribute>
      </xsl:element>
   </xsl:template>

</xsl:stylesheet>
