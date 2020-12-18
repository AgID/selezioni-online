/*
 * Copyright (C) 2020 Consiglio Nazionale delle Ricerche
 *       This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Affero General Public License as
 *        published by the Free Software Foundation, either version 3 of the
 *        License, or (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Affero General Public License for more details.
 *
 *       You should have received a copy of the GNU Affero General Public License
 *       along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package it.cnr.si.cool.jconon.agid.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import static feign.Util.checkNotNull;

public class ClientRequestInterceptor implements RequestInterceptor {

    private final String clientId;
    private final String clientSecret;

    public ClientRequestInterceptor(String clientId, String clientSecret) {
        checkNotNull(clientId, "clientId");
        checkNotNull(clientSecret, "clientSecret");
        this.clientId = clientId;
        this.clientSecret = clientSecret;

    }

    @Override
    public void apply(RequestTemplate template) {
        template.query("client_id", this.clientId);
        template.query("client_secret", this.clientSecret);
    }
}
