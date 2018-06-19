/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#ifndef WEEX_PROJECT_JSON_PARSER_H
#define WEEX_PROJECT_JSON_PARSER_H

#include <vector>
#include <string>

#include <json/json.h>

namespace WeexCore {

    class RenderObject;
    class RenderPage;

    RenderObject* Json2RenderObject(const Json::Value& jsonLayout, const Json::Value& jsonStyle, const std::string& pageId);
    RenderObject* JsonFile2RenderObject(const std::string& jsonLayoutPath, const std::string& jsonStylePath, const std::string& pageId);

    std::vector<std::pair<std::string, std::string>>* Json2Pairs(const Json::Value& json);
    std::vector<std::pair<std::string, std::string>>* JsonPath2Pairs(const std::string& path);

}

#endif // WEEX_PROJECT_JSON_PARSER_H
