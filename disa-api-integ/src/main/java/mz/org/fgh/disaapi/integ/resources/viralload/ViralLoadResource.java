/**
 *
 */
package mz.org.fgh.disaapi.integ.resources.viralload;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Service;

import mz.co.msaude.boot.frameworks.exception.BusinessException;
import mz.org.fgh.disaapi.core.viralload.config.AbstractUserContext;
import mz.org.fgh.disaapi.core.viralload.model.ViralLoad;
import mz.org.fgh.disaapi.core.viralload.model.ViralLoadStatus;
import mz.org.fgh.disaapi.core.viralload.service.ViralLoadQueryService;
import mz.org.fgh.disaapi.core.viralload.service.ViralLoadService;

/**
 * @author Stélio Moiane
 *
 */
@Path("viralloads")
@Service(ViralLoadResource.NAME)
public class ViralLoadResource extends AbstractUserContext {

	public static final String NAME = "mz.org.fgh.disaapi.integ.viralload.ViralLoadResource";

	@Inject
	private ViralLoadQueryService viralLoadQueryService;

	@Inject
	private ViralLoadService viralLoadService;

	private List<ViralLoad> viralLoads;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response findViralLoads(@QueryParam("locationCodes") final List<String> locationCodes)
			throws BusinessException {
		viralLoads = this.viralLoadQueryService.findByLocationCodeAndStatus(locationCodes);
		return Response.ok(viralLoads).build();

	}

	@GET
	@Path("viral-status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findViralLoadsByStatus(@QueryParam("locationCodes") final List<String> locationCodes,
			@QueryParam("viralLoadStatus") final ViralLoadStatus viralLoadStatus) throws BusinessException {
		viralLoads = this.viralLoadQueryService.findByStatus(locationCodes, viralLoadStatus);
		return Response.ok(viralLoads).build();
	}

	@GET
	@Path("viral-status-dates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findViralLoadsByStatusAndDates(@QueryParam("locationCodes") final List<String> locationCodes,
			@QueryParam("viralLoadStatus") final ViralLoadStatus viralLoadStatus,
			@QueryParam("startDate") final String strStartDate, @QueryParam("endDate") final String strEndDate)
			throws BusinessException {
		viralLoads = this.viralLoadQueryService.findByStatusAndDates(locationCodes, viralLoadStatus,
				convertToLocalDateTime(strStartDate), convertToLocalDateTime(strEndDate));
		return Response.ok(viralLoads).build();
	}

	@PUT
	@Path("not-processed")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateViralLoadNotProcessedViralLoad(
			@QueryParam("notProcessedNids") final List<String> notProcessedNids,
			@QueryParam("reasonForNotProcessing") final String reasonForNotProcessing) throws BusinessException {

		viralLoads = viralLoadQueryService.findViralLoadByNid(notProcessedNids);

		viralLoads.forEach(viralLoad -> {
			viralLoad.setNotProcessed();
			if (reasonForNotProcessing.equals("nid")) {
				viralLoad.setCauseNoNID();
			} else if (reasonForNotProcessing.equals("result")) {
				viralLoad.setCauseNoResult();
			}
			updateViralLoad(viralLoad);
		});

		return Response.ok(viralLoads).build();
	}

	private void updateViralLoad(ViralLoad viralLoad) {
		try {
			viralLoadService.updateViralLoad(getUserContext(), viralLoad);
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}

	@PUT
	@Path("processed")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateViralLoadProcessedViralLoad(@QueryParam("processedNids") final List<String> processedNids)
			throws BusinessException {

		viralLoads = viralLoadQueryService.findViralLoadByNid(processedNids);

		viralLoads.forEach(viralLoad -> {
			viralLoad.setProcessed();
			updateViralLoad(viralLoad);
		});

		return Response.ok(viralLoads).build();
	}

	@PUT
	@Path("pending")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateViralLoadPendingViralLoad(@QueryParam("pendingNids") final List<String> pendingNids)
			throws BusinessException {

		viralLoads = viralLoadQueryService.findViralLoadByNid(pendingNids);

		viralLoads.forEach(viralLoad -> {
			viralLoad.setPending();
			updateViralLoad(viralLoad);
		});

		return Response.ok(viralLoads).build();
	}

	private LocalDateTime convertToLocalDateTime(final String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		return LocalDateTime.parse(date, formatter);
	}

}
