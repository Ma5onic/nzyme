/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.rest.resources.authentication;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.CreateSessionRequest;
import horse.wtf.nzyme.rest.responses.authentication.SessionInformationResponse;
import horse.wtf.nzyme.rest.responses.authentication.SessionTokenResponse;
import horse.wtf.nzyme.security.sessions.Session;
import horse.wtf.nzyme.security.sessions.StaticHashAuthenticator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/api/authentication")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private static final Logger LOG = LogManager.getLogger(AuthenticationResource.class);

    @Inject
    private Nzyme nzyme;

    @Context
    SecurityContext securityContext;

    @POST
    @Path("/session")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSession(@NotNull CreateSessionRequest request) {
        StaticHashAuthenticator authenticator = new StaticHashAuthenticator(nzyme.getConfiguration());

        String username = request.username();
        String password = request.password();

        if (authenticator.authenticate(username, password)) {
            // Create session token.
            String token = Session.createToken(username, nzyme.getSigningKey());
            LOG.info("Creating session for user [{}]", username);
            return Response.status(Response.Status.CREATED).entity(SessionTokenResponse.create(token)).build();
        } else {
            LOG.warn("Failed login attempt for user [{}].", username);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @Path("/session/information")
    @Secured
    @GET
    public Response information() {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(nzyme.getSigningKey()).parseClaimsJws(securityContext.getUserPrincipal().getName());
            DateTime expiresAt = new DateTime(claims.getBody().getExpiration());

            return Response.ok(SessionInformationResponse.create(
                    SessionInformationResponse.STATUS.VALID,
                    Seconds.secondsBetween(DateTime.now(), expiresAt).getSeconds()
            )).build();
        } catch(Exception e) {
            return Response.ok(SessionInformationResponse.create(SessionInformationResponse.STATUS.INVALID, 0)).build();
        }
    }

}