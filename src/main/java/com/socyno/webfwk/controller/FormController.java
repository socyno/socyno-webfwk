package com.socyno.webfwk.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.github.reinert.jjschema.Attributes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmixutil.CommonUtil;
import com.socyno.base.bscmixutil.JsonUtil;
import com.socyno.base.bscmodel.R;
import com.socyno.base.bscservice.HttpUtil;
import com.socyno.stateform.abs.AbstractStateCommentAction;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.stateform.abs.DynamicStateForm;
import com.socyno.stateform.service.StateFormService;
import com.socyno.stateform.service.StateFormService.CommonStateFormRegister;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webbsc.ctxutil.HttpMessageConverter;
import com.socyno.webbsc.exception.PageNotFoundException;
import com.socyno.webbsc.service.jdbc.SimpleAttachmentService;
import com.socyno.webbsc.service.jdbc.SimpleLogService;

public class FormController {
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/logs/{formName}/{formId}", method = RequestMethod.GET)
    public R listLogs(@PathVariable("formName") String formName, @PathVariable("formId") long formId, Long fromLogIndex)
            throws Exception {
        return R.ok().setData(SimpleLogService.getInstance().queryLogExcludeOperationTypes(formName, formId,
                new String[] { AbstractStateCommentAction.getFormLogEvent() }, fromLogIndex));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/logs/{formName}/{formId}/{detailId}/detail", method = RequestMethod.GET)
    public R getLogDetail(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            @PathVariable("detailId") long detailId) throws Exception {
        return R.ok().setData(SimpleLogService.getInstance().getLogDetail(detailId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/comments/{formName}/{formId}", method = RequestMethod.GET)
    public R listComments(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            Long fromCommentId) throws Exception {
        return R.ok().setData(SimpleLogService.getInstance().queryLogIncludeOperationTypes(formName, formId,
                new String[] { AbstractStateCommentAction.getFormLogEvent() }, fromCommentId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/definition/{formName}", method = RequestMethod.GET)
    public R getFormDefinition(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(StateFormService.getFormDefinition(formName));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/list/{formName}", method = RequestMethod.POST)
    public R listForm(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = JsonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        return R.ok().setData(StateFormService.listForm(formName, data, null));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/list/{formName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotal(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = JsonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        return R.ok().setData(StateFormService.listFormWithTotal(formName, data, null));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/list/{formName}/{queryName}", method = RequestMethod.POST)
    public R listFormByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName,
            HttpServletRequest req) throws Exception {
        JsonElement data = JsonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        return R.ok().setData(StateFormService.listForm(formName, data, queryName));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/list/{formName}/{queryName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotalByName(@PathVariable("formName") String formName,
            @PathVariable("queryName") String queryName, HttpServletRequest req) throws Exception {
        JsonElement data = JsonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        return R.ok().setData(StateFormService.listFormWithTotal(formName, data, queryName));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/get/{formName}/{formId}", method = RequestMethod.GET)
    public R getForm(@PathVariable("formName") String formName, @PathVariable("formId") long formId) throws Exception {
        return R.ok().setData(StateFormService.getFormNoActions(formName, formId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/get/{formName}/{namedQuery}/{formId}", method = RequestMethod.GET)
    public R getNamedForm(@PathVariable("formName") String formName, @PathVariable("namedQuery") String namedQuery,
            @PathVariable("formId") long formId) throws Exception {
        AbstractStateForm formData = (StringUtils.isBlank(namedQuery) || "-".equals(namedQuery))
                ? StateFormService.getForm(formName, formId)
                : StateFormService.getForm(formName, namedQuery, formId);
        if (formData == null) {
            throw new PageNotFoundException();
        }
        return R.ok().setData(formData);
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/get/{formName}/actions/{actionName}", method = RequestMethod.GET)
    public R getFormActionDefinition(@PathVariable("formName") String formName, @PathVariable("actionName") String actionName)
            throws Exception {
        return R.ok().setData(StateFormService.getFormExtenalDefinition(formName, actionName));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/get/{formName}/{formId}/withActions", method = RequestMethod.GET)
    public R getFormWithActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(StateFormService.getFormWithActions(formName, formId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取指定表单的可执行操作清单（仅包含事件及显示名称）")
    @RequestMapping(value = "/get/{formName}/{formId}/actions/simple", method = RequestMethod.GET)
    public R getFormSimpleActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(StateFormService.getFormActionNames(formName, formId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取指定表单的可执行操作清单（包含事件及其详细定义）")
    @RequestMapping(value = "/get/{formName}/{formId}/actions/detail", method = RequestMethod.GET)
    public R getFormDetailActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(StateFormService.getFormActions(formName, formId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取指定表单的流程图绘制数据")
    @RequestMapping(value = "/flowchart/{formName}/data", method = RequestMethod.GET)
    public R getFormFlowChart(@PathVariable("formName") String formName, String formId, String unchanged)
            throws Exception {
        Long formLongId;
        if ((formLongId = CommonUtil.parsePositiveLong(formId, 0)) <= 0) {
            formLongId = null;
        }
        return R.ok().setData(
                StateFormService.parseFormFlowDefinition(formName, CommonUtil.parseBoolean(unchanged), formLongId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/trigger/{formName}/{formAction}", method = RequestMethod.POST)
    public R triggerAction(@PathVariable("formName") String formName, @PathVariable("formAction") String formAction,
            HttpServletRequest req) throws Exception {
        JsonElement jsonData = JsonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        JsonElement jsonForm;
        if (jsonData == null || !jsonData.isJsonObject() || (jsonForm = ((JsonObject) jsonData).get("form")) == null
                || !jsonForm.isJsonObject()) {
            throw new MessageException("请求数据不可识别.");
        }
        Class<?> actionResult = StateFormService.getActionReturnTypeClass(formName, formAction);
        Class<AbstractStateForm> actionClass = StateFormService.getActionFormTypeClass(formName, formAction);
        AbstractStateForm formData = HttpMessageConverter.toInstance(actionClass, jsonForm);
        if (formData instanceof DynamicStateForm) {
            ((DynamicStateForm) formData).setJsonData(jsonForm);
        }
        return R.ok().setData(StateFormService.triggerAction(formName, formAction, formData,
                JsonUtil.getJstring((JsonObject) jsonData, "message"), actionResult));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/prepare/{formName}/{formId}/{formAction}", method = RequestMethod.GET)
    public R triggerPrepare(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            @PathVariable("formAction") String formAction, HttpServletRequest req) throws Exception {
        List<NameValuePair> queryPairs;
        if ((queryPairs = HttpUtil.pairQueryString(req.getQueryString())) == null) {
            queryPairs = Collections.emptyList();
        }
        return R.ok().setData(StateFormService.triggerPrepare(formName, formAction, formId, queryPairs.toArray(new NameValuePair[0])));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/upload/{formName}", method = RequestMethod.POST)
    public R uploadCreate(@PathVariable("formName") String formName, MultipartHttpServletRequest req) throws Exception {
        if (!StateFormService.checkFormDefined(formName)) {
            throw new MessageException(String.format("给定的表单（%s）未注册", formName));
        }
        return R.ok().setData(SimpleAttachmentService.getDefault().upload(formName, req));
    }

    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/upload/{formName}/{formId}/{attachmentId}/download", method = RequestMethod.GET)
    public void uploadDownload(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            @PathVariable("attachmentId") long attachmentId, String preview, HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        if (CommonUtil.parseBoolean(preview)) {
            SimpleAttachmentService.getDefault().preview(attachmentId, req, resp);
            return;
        }
        SimpleAttachmentService.getDefault().download(attachmentId, req, resp);
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options", method = RequestMethod.GET)
    public R queryTypeFieldOptions(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String keyword, Long formId) throws Exception {
        return R.ok().setData(StateFormService.queryFieldTypeOptions(fieldTypeKey, keyword, formName, formId));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options/withQuery", method = RequestMethod.POST)
    public R queryTypeFieldOptionsWithQuery(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, HttpServletRequest req) throws Exception {
        String bodyJson = new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8");
        return R.ok().setData(StateFormService.queryFieldTypeOptions(formName, fieldTypeKey, bodyJson));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/values", method = RequestMethod.GET)
    public R queryTypeFieldValues(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String[] values) throws Exception {
        return R.ok().setData(StateFormService.queryFieldTypeValues(fieldTypeKey, values));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询给定的界面原型定义")
    @RequestMapping(value = "/view/{formName}/{formTypeKey}/construction", method = RequestMethod.GET)
    public R queryTypeFormConstruction(@PathVariable("formName") String formName,
            @PathVariable("formTypeKey") String formTypeKey, String freshCached) throws Exception {
        return R.ok().setData(StateFormService.queryFormTypeDefinition(formTypeKey));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "预览定制化的界面排版信息")
    @RequestMapping(value = "/view/{formName}/{formTypeKey}/construction/preview", method = RequestMethod.POST)
    public R previewTypeFormViewAttrs(@PathVariable("formName") String formName, @PathVariable("formTypeKey") String formTypeKey,
            @RequestBody List<Map<String, String>> defintion) throws Exception {
        return R.ok().setData(StateFormService.previewFieldCustomDefinition(formTypeKey, JsonUtil.toJson(defintion)));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询定制化的界面排版信息")
    @RequestMapping(value = "/setup/{formTypeKey}/viewattrs", method = RequestMethod.GET)
    public R queryTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey) throws Exception {
        return R.ok().setData(StateFormService.getFieldCustomDefinition(formTypeKey));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "更新定制化的界面排版信息")
    @RequestMapping(value = "/setup/{formTypeKey}/viewattrs/update", method = RequestMethod.POST)
    public R updateTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey,
            @RequestBody List<Map<String, String>> defintion) throws Exception {
        StateFormService.saveFieldCustomDefinition(formTypeKey, JsonUtil.toJson(defintion));
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取表单关联的外部界面模型清单")
    @RequestMapping(value = "/setup/{formName}/extraviews", method = RequestMethod.GET)
    public R queryFormExtraViews(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(StateFormService.queryFormExtraViews(formName));
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "保存表单关联的外部界面模型清单")
    @RequestMapping(value = "/setup/{formName}/extraviews/update", method = RequestMethod.POST)
    public R saveFormExtraViews(@PathVariable("formName") String formName, @RequestBody List<String> extraViews)
            throws Exception {
        StateFormService.saveFormExtraViews(formName, extraViews);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "注册的通用流程表单清单")
    @RequestMapping(value = "/setup/list", method = RequestMethod.GET)
    public R listDefinedForm() throws Exception {
        return R.ok().setData(StateFormService.listStateFormRegister());
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "注册的新的通用流程表单")
    @RequestMapping(value = "/setup/add", method = RequestMethod.POST)
    public R addDefinedForm(@RequestBody CommonStateFormRegister form) throws Exception {
        StateFormService.registerForm(form);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "更新通用流程表单配置信息")
    @RequestMapping(value = "/setup/update", method = RequestMethod.POST)
    public R updateDefinedForm(@RequestBody CommonStateFormRegister form) throws Exception {
        StateFormService.updateForm(form);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "删除已注册的通用流程表单")
    @RequestMapping(value = "/setup/delete/{formName}", method = RequestMethod.POST)
    public R removeDefinedForm(@PathVariable("formName") String formName) throws Exception {
        StateFormService.removeForm(formName);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "切换已注册的通用流程表单的状态（启动/禁用）")
    @RequestMapping(value = "/setup/toggle/{formName}", method = RequestMethod.POST)
    public R toggleForm(@PathVariable("formName") String formName) throws Exception {
        StateFormService.toggleForm(formName);
        return R.ok();
    }
}
