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
package org.hisp.dhis.sms;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hisp.dhis.message.MessageSender;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsListener;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.sms.incoming.SmsMessageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component( "org.hisp.dhis.sms.SmsConsumerThread" )
public class SmsConsumerThread
{
    private List<IncomingSmsListener> listeners;

    private final MessageQueue messageQueue;

    @Qualifier( "smsMessageSender" )
    private final MessageSender smsSender;

    private final IncomingSmsService incomingSmsService;

    public void spawnSmsConsumer()
    {
        IncomingSms message = messageQueue.get();

        while ( message != null )
        {
            log.info( "Received SMS: " + message.getText() );

            try
            {
                for ( IncomingSmsListener listener : listeners )
                {
                    if ( listener.accept( message ) )
                    {
                        listener.receive( message );
                        messageQueue.remove( message );
                        return;
                    }
                }

                log.warn( "No SMS command found in received data" );

                message.setStatus( SmsMessageStatus.UNHANDLED );

                smsSender.sendMessage( null, "No command found", message.getOriginator() );
            }
            catch ( Exception e )
            {
                e.printStackTrace();

                message.setStatus( SmsMessageStatus.FAILED );
                message.setParsed( false );
            }
            finally
            {
                messageQueue.remove( message );

                incomingSmsService.update( message );

                message = messageQueue.get();
            }
        }
    }

    @Autowired
    public void setListeners( List<IncomingSmsListener> listeners )
    {
        this.listeners = listeners;

        log.info( "Following listeners are registered: " + listeners );
    }
}
