package com.aldaviva.portfolio.social.api;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.CompoundStatus;
import com.aldaviva.portfolio.social.service.CompoundStatusService;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

@Path("social")
@Component
@Produces({ MediaType.APPLICATION_JSON })
public class SocialResource {

	@Inject private CompoundStatusService compoundStatusService;

	@GET
	public void getSocial(@Suspended final AsyncResponse res) throws SocialException, InterruptedException {
		Futures.addCallback(compoundStatusService.getCompoundStatus(), new FutureCallback<CompoundStatus>() {
			@Override
			public void onSuccess(final CompoundStatus compoundStatus) {
				res.resume(compoundStatus);
			}

			@Override
			public void onFailure(final Throwable t) {
				res.resume(t);
			}
		});
	}
}
