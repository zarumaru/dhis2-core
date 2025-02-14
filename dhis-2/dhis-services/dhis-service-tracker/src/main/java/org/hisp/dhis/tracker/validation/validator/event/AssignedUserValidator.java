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
package org.hisp.dhis.tracker.validation.validator.event;

import static org.hisp.dhis.tracker.validation.ValidationCode.E1118;
import static org.hisp.dhis.tracker.validation.ValidationCode.E1120;

import java.util.Optional;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.validation.Reporter;
import org.hisp.dhis.tracker.validation.Validator;

class AssignedUserValidator
    implements Validator<Event>
{
    @Override
    public void validate( Reporter reporter, TrackerBundle bundle, Event event )
    {
        if ( event.getAssignedUser() != null && !event.getAssignedUser().isEmpty() )
        {
            if ( assignedUserNotPresentInPreheat( bundle.getPreheat(), event ) )
            {
                reporter.addError( event, E1118, event.getAssignedUser().toString() );
            }
            if ( isNotEnabledUserAssignment( bundle.getPreheat(), event ) )
            {
                reporter.addWarning( event, E1120, event.getProgramStage() );
            }
        }
    }

    private boolean isNotEnabledUserAssignment( TrackerPreheat preheat, Event event )
    {
        Boolean userAssignmentEnabled = preheat.getProgramStage( event.getProgramStage() ).isEnableUserAssignment();

        return !Optional.ofNullable( userAssignmentEnabled )
            .orElse( false );
    }

    private boolean assignedUserNotPresentInPreheat( TrackerPreheat preheat, Event event )
    {
        return event.getAssignedUser().getUsername() == null ||
            preheat.getUserByUsername( event.getAssignedUser().getUsername() ).isEmpty();
    }
}
