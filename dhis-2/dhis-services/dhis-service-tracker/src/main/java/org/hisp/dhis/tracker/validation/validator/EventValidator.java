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
package org.hisp.dhis.tracker.validation.validator;

import static org.hisp.dhis.tracker.validation.validator.All.all;
import static org.hisp.dhis.tracker.validation.validator.Each.each;
import static org.hisp.dhis.tracker.validation.validator.Field.field;
import static org.hisp.dhis.tracker.validation.validator.Seq.seq;

import lombok.RequiredArgsConstructor;

import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.validation.Reporter;
import org.hisp.dhis.tracker.validation.Validator;
import org.springframework.stereotype.Component;

/**
 * Validator to validate all {@link Event}s in the {@link TrackerBundle}.
 */
@RequiredArgsConstructor
@Component( "org.hisp.dhis.tracker.validation.validator.EventValidator" )
public class EventValidator implements Validator<TrackerBundle>
{
    private final EventPreCheckUidValidator uidValidator;

    private final EventPreCheckExistenceValidator existenceValidator;

    private final EventPreCheckMandatoryFieldsValidator mandatoryFieldsValidator;

    private final EventPreCheckMetaValidator metaValidator;

    private final EventPreCheckUpdatableFieldsValidator updatableFieldsValidator;

    private final EventPreCheckDataRelationsValidator dataRelationsValidator;

    private final EventPreCheckSecurityOwnershipValidator securityOwnershipValidator;

    private final EventCategoryOptValidator categoryOptValidator;

    private final EventDateValidator dateValidator;

    private final EventGeoValidator geoValidator;

    private final EventNoteValidator noteValidator;

    private final EventDataValuesValidator dataValuesValidator;

    private final AssignedUserValidator assignedUserValidator;

    private final RepeatedEventsValidator repeatedEventsValidator;

    private Validator<TrackerBundle> eventValidator()
    {
        // @formatter:off
        return all(
                each( TrackerBundle::getEvents,
                    seq(
                            uidValidator,
                            existenceValidator,
                            mandatoryFieldsValidator,
                            metaValidator,
                            updatableFieldsValidator,
                            dataRelationsValidator,
                            securityOwnershipValidator,
                            all(
                                categoryOptValidator,
                                dateValidator,
                                geoValidator,
                                noteValidator,
                                dataValuesValidator,
                                assignedUserValidator
                            )
                    )
                ),
                field( TrackerBundle::getEvents, repeatedEventsValidator )
        );
        // @formatter:on
    }

    @Override
    public void validate( Reporter reporter, TrackerBundle bundle, TrackerBundle input )
    {
        eventValidator().validate( reporter, bundle, input );
    }

    @Override
    public boolean needsToRun( TrackerImportStrategy strategy )
    {
        return true; // this main validator should always run
    }
}
