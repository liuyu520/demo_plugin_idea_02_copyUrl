package com.kunlunsoft.copyurl.action;

import com.file.hw.props.GenericReadPropsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.kunlunsoft.common.dto.ConfigDto;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.Properties;

import static com.intellij.openapi.actionSystem.CommonDataKeys.*;
import static com.kunlunsoft.copyurl.common.SpringAnnotations.*;
import static com.kunlunsoft.copyurl.util.PropertiesUtil.getPropertyValue;
import static com.kunlunsoft.copyurl.util.PsiElementUtil.*;
import static com.kunlunsoft.copyurl.util.SpringUtil.containsSpringAnnotation;
import static com.kunlunsoft.copyurl.util.SpringUtil.isRestMethod;

/**
 * @author whuanghkl
 */
public class CopyRestUrlAction extends AnAction {

    public static final String GET = "GET";
    public static final String METHOD = "method";
    private static final String LOCALHOST = "http://localhost:";
    private static final String DEFAULT_PORT = "8080";
    private static final String REQUEST_METHOD_GET = "RequestMethod.GET";
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String SERVER_PORT = "server.port";
    private static final String SERVER_CONTEXT_PATH = "server.contextPath";

    private static String getMethodUrl(String methodUrl, String methodName) {
        String listFilterUrl = "/listfilter/json";
        switch (methodName) {
            case "beforeList":
            case "listTODO":
                methodUrl = "/list/json";
                break;
            case "buildDaoFilterChain":
            case "buildQueryFilterChain":
                methodUrl = listFilterUrl;
                break;
            case "detailTODO":
                methodUrl = "/2/json";
                break;
            case "beforeSave":
                methodUrl = "/add/json";
                break;
            case "deleteTODO":
                methodUrl = "/delete/3/json";
                break;
        }
        return methodUrl;
    }

    @NotNull
    private static ConfigDto getConfigDto() {
        ConfigDto configDto = new ConfigDto();
        String configPath = System.getProperty("user.home") + "/.idea/idea_plugin.properties";
        try {

            Properties properties = GenericReadPropsUtil.getProperties(false
                    , configPath);
            if (null != properties) {
                if (properties.containsKey("with_port")) {
                    configDto.setWithPort(Boolean.parseBoolean(properties.getProperty("with_port")));
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return configDto;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiElement psiElement = e.getData(PSI_ELEMENT);
        String queryList = "";

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            PsiClass psiClass = psiMethod.getContainingClass();

            PsiModifierList classModifierList = psiClass.getModifierList();
            PsiModifierList methodModifierList = psiMethod.getModifierList();

            String classUrl = getUrl(classModifierList, REQUEST_MAPPING_QUALIFIED_NAME);

            String methodUrl = "";
            Project project = e.getProject();
            methodUrl = getMethodUrl(methodModifierList);
            queryList = createParameters(psiMethod.getParameterList());
            if (null == methodUrl || methodUrl.length() == 0) {
                String methodName = psiMethod.getName();
//                Messages.showMessageDialog(project, methodName, "接口路径", Messages.getInformationIcon());
                methodUrl = getMethodUrl(methodUrl, methodName);
            }

            ConfigDto configDto = getConfigDto();
//            Messages.showMessageDialog(project, configPath+","+HWJacksonUtils.getJsonP(configDto), "接口路径", Messages.getInformationIcon());
            StringBuilder url = new StringBuilder();
            String port2 = getPortAndContextPath(project);
            if (configDto.isWithPort() && null != port2 && port2.length() > 0) {
                url.append(LOCALHOST);
                url.append(port2);
            }

            // /redis/long/map/add/json
            url.append(classUrl);
            if (!classUrl.endsWith("/")
                    && (!methodUrl.startsWith("/"))) {
                url.append("/");
            }
            url.append(methodUrl);
            if (null != queryList && (!queryList.isEmpty())) {
                url.append("?");
                //id=X&key=X&value=X
                url.append(queryList);
            }

            String fullUrl = url.toString()
                    .replace("///", "/")
                    .replace("//", "/");
            CopyPasteManager.getInstance().setContents(new StringSelection(queryList));
            CopyPasteManager.getInstance().setContents(new StringSelection(fullUrl));
//            ToastMessage.toast("copy success",1000);
//            Messages.showMessageDialog(project, fullUrl, "接口路径", Messages.getInformationIcon());
        }
    }

    private String getMethodUrl(PsiModifierList methodModifierList) {
        String methodUrl = null;
        if (containsSpringAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, REQUEST_MAPPING_QUALIFIED_NAME);

            String httpMethod = getAnnotationValue(methodModifierList, METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

            /*if (httpMethod.equalsIgnoreCase(GET) || httpMethod.equalsIgnoreCase(REQUEST_METHOD_GET) || httpMethod.isEmpty()) {

            }*/

        } else if (containsSpringAnnotation(GET_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, GET_MAPPING_QUALIFIED_NAME);
//                queryList = createQueryWithParameters(psiMethod.getParameterList());
        } else if (containsSpringAnnotation(POST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, POST_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(PATCH_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, PATCH_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(DELETE_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, DELETE_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(PUT_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrl = getUrl(methodModifierList, PUT_MAPPING_QUALIFIED_NAME);
        }
        return methodUrl;
    }

    private String getPortAndContextPath(Project project) {
        StringBuilder url = new StringBuilder("");
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, APPLICATION_PROPERTIES,
                GlobalSearchScope.allScope(project));

        if (filesByName.length > 0) {
            PsiFile psiFile = filesByName[0];
            String port = getPropertyValue(psiFile.getText(), SERVER_PORT);

            url.append(port.isEmpty() ? DEFAULT_PORT : port);
            url.append(getPropertyValue(psiFile.getText(), SERVER_CONTEXT_PATH));
        }
        return url.toString();
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PROJECT);
        Editor editor = e.getData(EDITOR);

        PsiElement psiElement = e.getData(PSI_ELEMENT);

        boolean available = false;

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            PsiClass psiClass = psiMethod.getContainingClass();

            available = (isRestMethod(psiMethod.getModifierList())
                    && (containsSpringAnnotation(CONTROLLER, psiClass.getModifierList()) ||
                    containsSpringAnnotation(REST_CONTROLLER, psiClass.getModifierList())));
        }

        e.getPresentation().setVisible(project != null && editor != null && available);
    }
}