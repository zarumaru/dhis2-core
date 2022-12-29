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

import java.util.List;

import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.programstageworkinglistdefinition.ProgramStageWorkingListDefinition;
import org.hisp.dhis.programstageworkinglistdefinition.ProgramStageWorkingListDefinitionService;
import org.hisp.dhis.schema.descriptors.ProgramStageWorkingListDefinitionSchemaDescriptor;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@OpenApi.Tags( "tracker" )
@Controller
@RequestMapping( value = ProgramStageWorkingListDefinitionSchemaDescriptor.API_ENDPOINT )
@ApiVersion( include = { DhisApiVersion.ALL, DhisApiVersion.DEFAULT } )
public class ProgramStageWorkingListController
    extends AbstractCrudController<ProgramStageWorkingListDefinition>
{
    private final ProgramStageWorkingListDefinitionService workingListDefinitionService;

    public ProgramStageWorkingListController( ProgramStageWorkingListDefinitionService workingListDefinitionService )
    {
        this.workingListDefinitionService = workingListDefinitionService;
    }

    @Override
    public void preCreateEntity( ProgramStageWorkingListDefinition workingListDefinition )
    {
        List<String> errors = workingListDefinitionService.validate( workingListDefinition );
        if ( !errors.isEmpty() )
        {
            throw new IllegalQueryException( errors.toString() );
        }
    }

    @Override
    public void preUpdateEntity( ProgramStageWorkingListDefinition oldWorkingListDefinition,
        ProgramStageWorkingListDefinition newWorkingListDefinition )
    {
        List<String> errors = workingListDefinitionService.validate( newWorkingListDefinition );
        if ( !errors.isEmpty() )
        {
            throw new IllegalQueryException( errors.toString() );
        }
    }
}
