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

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Luciano Fiandesio
 */
@Component( "workContextTrackedEntityInstancesSupplier" )
public class TrackedEntityInstanceSupplier extends AbstractSupplier<Map<String, TrackedEntityInstance>>
{
    public TrackedEntityInstanceSupplier( NamedParameterJdbcTemplate jdbcTemplate )
    {
        super( jdbcTemplate );
    }

    @Override
    public Map<String, TrackedEntityInstance> get( List<Event> events )
    {
        if ( events == null )
        {
            return new HashMap<>();
        }
        // @formatter:off
        // Collect all the org unit uids to pass as SQL query argument
        Set<String> teiUids = events.stream()
                .filter( e -> e.getTrackedEntityInstance() != null )
                .map( Event::getTrackedEntityInstance ).collect( Collectors.toSet() );
        // @formatter:on

        if ( isEmpty( teiUids ) )
        {
            return new HashMap<>();
        }

        // Create a map: tei uid -> List [event uid]
        Multimap<String, String> teiToEvent = HashMultimap.create();
        for ( Event event : events )
        {
            teiToEvent.put( event.getTrackedEntityInstance(), event.getUid() );
        }

        final String sql = "select tei.trackedentityinstanceid, tei.uid, tei.code from trackedentityinstance tei where tei.uid in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue( "ids", teiUids );

        return jdbcTemplate.query( sql, parameters, ( ResultSet rs ) -> {
            Map<String, TrackedEntityInstance> results = new HashMap<>();

            while ( rs.next() )
            {
                TrackedEntityInstance tei = new TrackedEntityInstance();
                tei.setId( rs.getLong( "trackedentityinstanceid" ) );
                tei.setUid( rs.getString( "uid" ) );
                tei.setCode( rs.getString( "code" ) );
                for ( String event : teiToEvent.get( tei.getUid() ) )
                {
                    results.put( event, tei );
                }

            }
            return results;
        } );
    }
}
