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
package org.hisp.dhis.tracker.validation.validator.enrollment;

import static org.hisp.dhis.tracker.validation.validator.ValidationUtils.addIssuesToReporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.programrule.ProgramRuleIssue;
import org.hisp.dhis.tracker.programrule.implementers.enrollment.ActionRule;
import org.hisp.dhis.tracker.programrule.implementers.enrollment.RuleActionEnrollmentValidator;
import org.hisp.dhis.tracker.validation.Reporter;
import org.hisp.dhis.tracker.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Enrico Colasante
 */
@Component( "org.hisp.dhis.tracker.validation.validator.enrollment.RuleEngineValidator" )
class RuleEngineValidator
    implements Validator<Enrollment>
{
    private List<RuleActionEnrollmentValidator> validators;

    @Autowired( required = false )
    public void setValidators( List<RuleActionEnrollmentValidator> validators )
    {
        this.validators = validators;
    }

    @Override
    public void validate( Reporter reporter, TrackerBundle bundle, Enrollment enrollment )
    {
        List<ActionRule> actionRules = bundle.getEnrollmentActionRules().getOrDefault( enrollment,
            Collections.emptyList() );

        if ( actionRules.isEmpty() )
        {
            return;
        }

        List<ProgramRuleIssue> programRuleIssues = new ArrayList<>();

        // TODO: I would like to find an elegant solution insteado of the validator filter.
        // We have a list of action rules that are of different types, hence they need
        // to be processed by different validators.
        // A validator accepts only the type that it can process.
        // We need to call every validator with the a list of action fo the type (and only the type) that it can process
        for ( RuleActionEnrollmentValidator validator : validators )
        {
            programRuleIssues
                .addAll( validator.validateEnrollment( bundle, validator.filter( actionRules ), enrollment ) );
        }

        addIssuesToReporter( reporter, enrollment, programRuleIssues );
    }
}