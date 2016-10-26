/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.processor;

import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;

/**
 * Timeline processor.
 *
 * <ul>
 * <li>Shows timeline (/timeline), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.8, Oct 26, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class TimelineProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TimelineProcessor.class.getName());

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Shows timeline.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/timeline", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTimeline(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);;
        context.setRenderer(renderer);
        renderer.setTemplateName("timeline.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.SELECTED, Common.TIMELINE);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        filler.fillRandomArticles(avatarViewMode, dataModel);
        filler.fillSideHotArticles(avatarViewMode, dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);

        dataModel.put("timelineCnt", Symphonys.getInt("timelineCnt"));
        dataModel.put(Common.TIMELINES, timelineMgmtService.getTimelines());
    }
}
