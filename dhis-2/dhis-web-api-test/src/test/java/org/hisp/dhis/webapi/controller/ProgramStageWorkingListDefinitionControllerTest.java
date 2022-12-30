/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.utils.Assertions.assertContains;
import static org.hisp.dhis.web.WebClientUtils.assertStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.web.HttpStatus;
import org.hisp.dhis.webapi.DhisControllerConvenienceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgramStageWorkingListDefinitionControllerTest extends DhisControllerConvenienceTest
{

    private String programId;

    private String programStageId;

    private String ouId;

    private String attributeId;

    @BeforeEach
    void setUp()
    {
        programId = assertStatus( HttpStatus.CREATED, POST( "/programs/",
            "{'name': 'ProgramTest', 'shortName': 'ProgramTest', 'programType': 'WITHOUT_REGISTRATION'}" ) );

        programStageId = assertStatus( HttpStatus.CREATED,
            POST( "/programStages/", "{'name': 'ProgramStageTest', 'program':" + "{'id': '" + programId + "'}}" ) );

        ouId = assertStatus( HttpStatus.CREATED,
            POST( "/organisationUnits/", "{'name':'My Unit', 'shortName':'OU1', 'openingDate': '2020-01-01'}" ) );

        attributeId = assertStatus( HttpStatus.CREATED,
            POST( "/trackedEntityAttributes/",
                "{'name':'attrA', 'shortName':'attrA', 'valueType':'TEXT', 'aggregationType':'NONE'}" ) );
    }

    @Test
    void shouldReturnWorkingListIdDefinitionWhenWorkingListCreated()
    {
        String workingListId = createWorkingListDefinition( "Test working list" );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithoutName()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            "{'program': {'id': '" + programId + "'}, 'programStage': {'id': '" + programStageId + "'}}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[No name specified for the working list definition.]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithoutProgramId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            "{'programStage': {'id': '" + programStageId + "'}, 'name':'Test'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[No program specified for the working list definition.]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithNonExistentProgramId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            "{'program': {'id': 'madeUpProgramId'}, 'programStage': {'id': '" + programStageId + "'}, 'name':'Test'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Program is specified but does not exist", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithoutProgramStageId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            "{'program': {'id': '" + programId + "'}, 'name':'Test'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[No program stage specified for the working list definition.]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithNonExistentProgramStageId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            "{'program': {'id': '" + programId + "'}, 'programStage': {'id': 'madeUpProgramStageId'}, 'name':'Test'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Program stage is specified but does not exist", response.error().getMessage() );
    }

    @Test
    void shouldReturnWorkingListIdWhenCreatingWorkingListDefinitionWithEventStatus()
    {
        String workingListId = assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'status':'ACTIVE'}" ) ) );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldReturnIdWhenCreatingWorkingListDefinitionWithOrgUnit()
    {
        String workingListId = assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'organisationUnit':'" + ouId + "'}" ) ) );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithNonExistentOrgUnit()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'organisationUnit':'madeUpOrgUnit'}}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Org unit is specified but does not exist", response.error().getMessage() );
    }

    @Test
    void shouldReturnIdWhenCreatingWorkingListDefinitionWithCorrectAbsoluteDate()
    {
        String workingListId = assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            createPostRequestBody(
                "{'eventDate':{'type':'ABSOLUTE','startDate':'2020-03-01','endDate':'2022-12-30'}}" ) ) );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithStartDateButNoEndDate()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'eventDate':{'type':'ABSOLUTE','startDate':'2023-03-01'}}}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Start date or end date not specified with ABSOLUTE date period",
            response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithStartDateAfterEndDate()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions", createPostRequestBody(
            "{'eventDate':{'type':'ABSOLUTE','startDate':'2023-03-01','endDate':'2020-12-30'}}}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Start date can't be after end date", response.error().getMessage() );
    }

    @Test
    void shouldReturnIdWhenCreatingWorkingListDefinitionWithAssignedUserAndUserModeProvided()
    {
        String workingListId = assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'assignedUsers':['DXyJmlo9rge'], 'assignedUserMode':'PROVIDED'}" ) ) );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithoutAssignedUserAndUserModeProvided()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{'assignedUserMode':'PROVIDED'}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[Assigned Users cannot be empty with PROVIDED assigned user mode]",
            response.error().getMessage() );
    }

    @Test
    void shouldReturnIdWhenCreatingWorkingListDefinitionWithDataFiltersAndExistingAttribute()
    {
        String workingListId = assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            createPostRequestBody(
                "{ 'dataFilters':[{'dataItem': '" + attributeId + "', 'ge': '10', 'le': '20'}]}" ) ) );

        assertFalse( workingListId.isEmpty(), "Expected working list id, but got nothing instead" );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithDataFiltersButMissingAttributeId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{ 'dataFilters':[{'dataItem': '', 'ge': '10', 'le': '20'}]}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[Attribute Uid is missing in filter]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenCreatingWorkingListDefinitionWithDataFiltersAndNonExistentAttributeId()
    {
        HttpResponse response = POST( "/programStageWorkingListDefinitions",
            createPostRequestBody( "{ 'dataFilters':[{'dataItem': 'madeUpAttributeId', 'ge': '10', 'le': '20'}]}" ) );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "No tracked entity attribute found", response.error().getMessage() );
    }

    @Test
    void shouldReturnAllWorkingListsDefinitionWhenWorkingListsRequested()
    {
        String workingListId1 = createWorkingListDefinition( "Test working list 1" );
        String workingListId2 = createWorkingListDefinition( "Test working list 2" );

        String response = GET( "/programStageWorkingListDefinitions?fields=id" ).content().toString();

        assertTrue( response.contains( workingListId1 ),
            "The working list id: " + workingListId1 + " is not present in the response" );
        assertTrue( response.contains( workingListId2 ),
            "The working list id: " + workingListId2 + " is not present in the response" );
    }

    @Test
    void shouldUpdateWorkingListDefinitionWhenUpdateRequested()
    {
        String workingListId = createWorkingListDefinition( "Test working list to update" );

        String updatedName = "Updated working list";
        assertStatus( HttpStatus.OK, PUT( "/programStageWorkingListDefinitions/" + workingListId,
            "{'program': {'id': '" + programId + "'}, 'programStage': {'id': '" + programStageId + "'}, 'name':'"
                + updatedName + "'}" ) );

        String response = GET( "/programStageWorkingListDefinitions/{id}", workingListId ).content().toString();
        assertTrue( response.contains( updatedName ),
            "Could not find the working list name: " + updatedName + " in the response" );
    }

    @Test
    void shouldFailWhenUpdatingWorkingListDefinitionWithoutProgramId()
    {
        String workingListId = createWorkingListDefinition( "Test working list to update" );

        String updatedName = "Updated working list";
        HttpResponse response = PUT( "/programStageWorkingListDefinitions/" + workingListId,
            "{'programStage': {'id': '" + programStageId + "'}, 'name':'" + updatedName + "'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[No program specified for the working list definition.]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenUpdatingWorkingListDefinitionWithNonExistentProgramId()
    {
        String workingListId = createWorkingListDefinition( "Test working list to update" );

        String updatedName = "Updated working list";
        HttpResponse response = PUT( "/programStageWorkingListDefinitions/" + workingListId,
            "{'program': {'id': 'madeUpProgramId'}, 'programStage': {'id': '" + programStageId + "'}, 'name':'"
                + updatedName + "'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Program is specified but does not exist", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenUpdatingWorkingListDefinitionWithoutProgramStageId()
    {
        String workingListId = createWorkingListDefinition( "Test working list to update" );

        String updatedName = "Updated working list";
        HttpResponse response = PUT( "/programStageWorkingListDefinitions/" + workingListId,
            "{'program': {'id': '" + programId + "'}, 'name':'" + updatedName + "'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertEquals( "[No program stage specified for the working list definition.]", response.error().getMessage() );
    }

    @Test
    void shouldFailWhenUpdatingWorkingListDefinitionWithNonExistentProgramStageId()
    {
        String workingListId = createWorkingListDefinition( "Test working list to update" );

        String updatedName = "Updated working list";
        HttpResponse response = PUT( "/programStageWorkingListDefinitions/" + workingListId,
            "{'program': {'id': '" + programId + "'}, 'programStage': {'id': 'madeUpProgramStageId'}, 'name':'"
                + updatedName + "'}" );

        assertEquals( HttpStatus.CONFLICT, response.status() );
        assertContains( "Program stage is specified but does not exist", response.error().getMessage() );
    }

    @Test
    void shouldDeleteWorkingListDefinitionWhenDeleteRequested()
    {
        String workingListId = createWorkingListDefinition( "Test working to delete" );

        HttpResponse response = DELETE( "/programStageWorkingListDefinitions/" + workingListId );
        assertEquals( HttpStatus.OK, response.status() );
    }

    private String createWorkingListDefinition( String workingListName )
    {
        return assertStatus( HttpStatus.CREATED, POST( "/programStageWorkingListDefinitions",
            "{'program': {'id': '" + programId + "'}, 'programStage': {'id': '" + programStageId + "'}, 'name':'"
                + workingListName + "'}" ) );
    }

    private String createPostRequestBody( String programStageQueryCriteria )
    {
        return "{'program': {'id': '" + programId + "'}, 'programStage': {'id': '" + programStageId
            + "'}, 'name':'wl name', 'programStageQueryCriteria':" + programStageQueryCriteria + "}";
    }
}