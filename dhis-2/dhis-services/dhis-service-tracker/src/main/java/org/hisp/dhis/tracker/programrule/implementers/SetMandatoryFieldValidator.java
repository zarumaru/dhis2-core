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
package org.hisp.dhis.tracker.programrule.implementers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.programrule.EventActionRule;
import org.hisp.dhis.tracker.programrule.IssueType;
import org.hisp.dhis.tracker.programrule.ProgramRuleIssue;
import org.hisp.dhis.tracker.validation.ValidationCode;
import org.hisp.dhis.tracker.validation.validator.ValidationUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * This implementer check if a field is not empty in the {@link TrackerBundle}
 *
 * @Author Enrico Colasante
 */
@Component
public class SetMandatoryFieldValidator implements RuleActionImplementer
{
    @Override
    public RuleActionType getActionType()
    {
        return RuleActionType.MANDATORY_VALUE;
    }

    @Override
    public List<ProgramRuleIssue> validateEvent( TrackerBundle bundle, List<EventActionRule> eventActionRules,
        Event event )
    {
        return checkMandatoryDataElement( event, eventActionRules, bundle );
    }

    private List<ProgramRuleIssue> checkMandatoryDataElement( Event event, List<EventActionRule> actionRules,
        TrackerBundle bundle )
    {
        TrackerPreheat preheat = bundle.getPreheat();
        ProgramStage programStage = preheat.getProgramStage( event.getProgramStage() );
        TrackerIdSchemeParams idSchemes = preheat.getIdSchemes();

        Map<MetadataIdentifier, EventActionRule> mandatoryDataElementsByActionRule = actionRules.stream()
            .collect( Collectors.toMap( r -> idSchemes.toMetadataIdentifier( preheat.getDataElement( r.getField() ) ),
                Function.identity() ) );

        return ValidationUtils.validateMandatoryDataValue( programStage, event,
            Lists.newArrayList( mandatoryDataElementsByActionRule.keySet() ) )
            .stream()
            .map( e -> new ProgramRuleIssue( mandatoryDataElementsByActionRule.get( e ).getRuleUid(),
                ValidationCode.E1301,
                Lists.newArrayList( e.getIdentifierOrAttributeValue() ), IssueType.ERROR ) )
            .collect( Collectors.toList() );
    }
}
