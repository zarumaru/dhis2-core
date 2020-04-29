package org.hisp.dhis.dxf2.events.event.context;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Luciano Fiandesio
 */
public class OrganisationUnitSupplierTest extends AbstractSupplierTest<OrganisationUnit>
{
    private OrganisationUnitSupplier subject;

    @Before
    public void setUp()
    {
        this.subject = new OrganisationUnitSupplier( jdbcTemplate );
    }

    @Test
    public void handleNullEvents()
    {
        assertNotNull( subject.get( null ) );
    }

    public void verifySupplier()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "organisationunitid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "ALFA" );
        when( mockResultSet.getString( "path" ) ).thenReturn( "/aaaa/bbbb/cccc/abcded" );
        when( mockResultSet.getInt( "hierarchylevel" ) ).thenReturn( 4 );

        // create event to import
        Event event = new Event();
        event.setUid( CodeGenerator.generateUid() );
        event.setOrgUnit( "abcded" );

        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );

        Map<String, OrganisationUnit> map = subject.get( Collections.singletonList( event ) );

        OrganisationUnit organisationUnit = map.get( event.getUid() );
        assertThat( organisationUnit, is( notNullValue() ) );
        assertThat( organisationUnit.getId(), is( 100L ) );
        assertThat( organisationUnit.getUid(), is( "abcded" ) );
        assertThat( organisationUnit.getCode(), is( "ALFA" ) );
        assertThat( organisationUnit.getPath(), is( "/aaaa/bbbb/cccc/abcded" ) );
        assertThat( organisationUnit.getHierarchyLevel(), is( 4 ) );
    }
}