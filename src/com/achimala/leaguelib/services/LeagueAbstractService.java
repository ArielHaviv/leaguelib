/*
 *  This file is part of LeagueLib.
 *  LeagueLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  LeagueLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with LeagueLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.achimala.leaguelib.services;

import com.gvaneyck.rtmp.TypedObject;
import com.achimala.leaguelib.connection.*;
import com.achimala.leaguelib.errors.*;
import com.achimala.util.Callback;
import java.io.IOException;

public abstract class LeagueAbstractService {
    protected LeagueConnection _connection = null;
    
    public LeagueAbstractService(LeagueConnection connection) {
        _connection = connection;
    }
    
    private TypedObject handleResult(TypedObject result) throws LeagueException {
        if(result.get("result").equals("_error")) {
            System.err.println(result);
            throw new LeagueException(LeagueErrorCode.RTMP_ERROR, _connection.getInternalRTMPClient().getErrorMessage(result));
        }
        return result.getTO("data");
    }
    
    protected TypedObject call(String method, Object arguments) throws LeagueException {
        try {
            int id = _connection.getInternalRTMPClient().invoke(getServiceName(), method, arguments);
            TypedObject result = _connection.getInternalRTMPClient().getResult(id);
            return handleResult(result);
        } catch(IOException ex) {
            throw new LeagueException(LeagueErrorCode.NETWORK_ERROR, ex.getMessage());
        }
    }
    
    protected void callAsynchronously(String method, Object arguments, final Callback<TypedObject> callback) {
        try {
            _connection.getInternalRTMPClient().invokeWithCallback(getServiceName(), method, arguments, new com.gvaneyck.rtmp.Callback() {
                public void callback(TypedObject result) {
                    try {
                        callback.onCompletion(handleResult(result));
                    } catch(LeagueException ex) {
                        callback.onError(ex);
                    }
                }
            });
        } catch(IOException ex) {
            callback.onError(ex);
        }
    }
    
    public abstract String getServiceName();
    
    public String toString() {
        return String.format("<Service:%s>", getServiceName());
    }
}