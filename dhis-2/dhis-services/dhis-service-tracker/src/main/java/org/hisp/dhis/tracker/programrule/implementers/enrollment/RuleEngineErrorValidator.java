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
package org.hisp.dhis.tracker.programrule.implementers.enrollment;

import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.programrule.IssueType;
import org.hisp.dhis.tracker.programrule.ProgramRuleIssue;
import org.hisp.dhis.tracker.validation.ValidationCode;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * This implementer log as a warning any error raised by rule engine execution
 *
 * @Author Enrico Colasante
 */
@Component( "org.hisp.dhis.tracker.programrule.implementers.enrollment.RuleEngineErrorImplementer" )
public class RuleEngineErrorValidator implements RuleActionEnrollmentValidator<SyntaxErrorActionRule>
{
    @Override
    public List<SyntaxErrorActionRule> filter( List<ActionRule> actionRules )
    {
        return actionRules
            .stream()
            .filter( a -> a instanceof SyntaxErrorActionRule )
            .map( a -> (SyntaxErrorActionRule) a )
            .collect( Collectors.toList() );
    }

    @Override
    public List<ProgramRuleIssue> validateEnrollment( TrackerBundle bundle,
        List<SyntaxErrorActionRule> enrollmentActionRules, Enrollment enrollment )
    {
        return enrollmentActionRules.stream()
            .map( e -> new ProgramRuleIssue( e.getRuleUid(), ValidationCode.E1300,
                Lists.newArrayList( e.getData() ), IssueType.WARNING ) )
            .collect( Collectors.toList() );
    }
}
