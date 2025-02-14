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
package org.hisp.dhis.web.embeddedjetty;

import static org.hisp.dhis.web.embeddedjetty.RootPageServlet.session;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.user.CurrentUserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
public class GetAppMenuServlet
    extends HttpServlet
{

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws IOException,
        ServletException
    {
        Object springSecurityContext = session().getAttribute( "SPRING_SECURITY_CONTEXT" );

        if ( springSecurityContext != null )
        {
            SecurityContextImpl context = (SecurityContextImpl) session().getAttribute(
                "SPRING_SECURITY_CONTEXT" );

            Authentication authentication = context.getAuthentication();
            CurrentUserDetailsImpl principal = (CurrentUserDetailsImpl) authentication.getPrincipal();
            String username = principal.getUsername();

            resp.setContentType( "application/json" );
            resp.setStatus( HttpServletResponse.SC_OK );

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
                "/api/apps/menu" + "?username=" + username );

            dispatcher.include( req, resp );
        }
        else
        {
            resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        }

    }
}
