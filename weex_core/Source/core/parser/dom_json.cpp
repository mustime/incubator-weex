/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#include <sstream>
#include <core/render/node/render_object.h>
#include <core/render/page/render_page.h>
#include <core/render/node/factory/render_creator.h>
#include "dom_json.h"

namespace WeexCore
{

    std::string* getStringFromFile(const std::string& path)
    {
        std::string* result = new std::string();
        FILE* fp = fopen(path.c_str(), "rt");
        if (fp)
        {
            fseek(fp, 0, SEEK_END);
            size_t size = ftell(fp);
            fseek(fp, 0, SEEK_SET);
            
            result->reserve(size);
            char* buffer = (char*)malloc(sizeof(char) * 1024);
            while ((size = fread(buffer, sizeof(unsigned char), 1024, fp)) > 0)
                result->append(buffer, size);
            fclose(fp);
        }
        return result;
    }

    RenderObject* parseJson2RenderObject(const Json::Value& jsonLayout, const Json::Value& jsonStyle, const std::string& pageId, RenderObject* parent, int index)
    {
        if (jsonLayout.isNull())
            return nullptr;

        RenderObject* render = nullptr;
        std::string view = jsonLayout["view"].asString();
        if (!view.empty())
        {
            std::string id = jsonLayout["id"].asString();
            if (id.empty())
            {
                std::stringstream ss;
                ss << (parent ? parent->ref() + "@" : "") << "ANON_" << view << index;
                ss >> id;
            }
            render = (RenderObject*)RenderCreator::GetInstance()->CreateRender(view, id);
            render->set_page_id(pageId);
            if (parent != nullptr)
                parent->AddRenderObject(index, render);

            auto applyStyleJson = [](RenderObject* render, const Json::Value& jsonStyle)
                {
                    for (auto iter = jsonStyle.begin(); iter != jsonStyle.end(); ++ iter)
                        render->AddStyle(iter.name(), jsonStyle[iter.name()].asString());
                };

            auto isReservedAttr = [](const std::string& name)
                {
                    const static auto RESERVED_ATTR_KEYS = { "view", "id", "class", "style", "subviews" };
                    for (const auto& key : RESERVED_ATTR_KEYS)
                        if (name == key) return true;
                    return false;
                };

            // apply class styles
            if (jsonLayout["class"].isArray())
            {
                for (size_t i = 0; i < jsonLayout["class"].size(); ++ i)
                    applyStyleJson(render, jsonStyle[jsonLayout["class"][i].asString()]);
            }
            else if (jsonLayout["class"].isString())
                applyStyleJson(render, jsonStyle[jsonLayout["class"].asString()]);
            // overrided styles
            applyStyleJson(render, jsonLayout["style"]);

            // apply attributes
            for (auto iter = jsonLayout.begin(); iter != jsonLayout.end(); ++ iter)
            {
                if (!isReservedAttr(iter.name()))
                {
                    if (iter.name()[0] != '@')
                        render->AddAttr(iter.name(), jsonLayout[iter.name()].asString());
                    else
                        render->AddEvent(iter.name());
                }
            }

            // handle subviews
            Json::Value jsonSubviews = jsonLayout["subviews"];
            if (jsonSubviews.isArray())
            {
                for (size_t i = 0; i < jsonSubviews.size(); ++ i)
                    parseJson2RenderObject(jsonSubviews[i], jsonStyle, pageId, render, i);
            }
        }

        if (render != nullptr)
        {
            render->ApplyDefaultStyle();
            render->ApplyDefaultAttr();
        }
        return render;
    }

    RenderObject* Json2RenderObject(const Json::Value& jsonLayout, const Json::Value& jsonStyle, const std::string& pageId)
    {
        return parseJson2RenderObject(jsonLayout, jsonStyle, pageId, nullptr, 0);
    }

    RenderObject* JsonFile2RenderObject(const std::string& jsonLayoutPath, const std::string& jsonStylePath, const std::string& pageId)
    {
        RenderObject* object = nullptr;
        Json::Value jsonStyle;
        Json::Value jsonLayout;
        std::string* jsonStyleStr = getStringFromFile(jsonStylePath);
        std::string* jsonLayoutStr = getStringFromFile(jsonLayoutPath);
        Json::Reader jsonReader;
        if (jsonReader.parse(*jsonStyleStr, jsonStyle) &&
            jsonReader.parse(*jsonLayoutStr, jsonLayout))
        {
            delete jsonStyleStr;
            delete jsonLayoutStr;
            object = Json2RenderObject(jsonLayout, jsonStyle, pageId);
        }
        return object;
    }

    std::vector<std::pair<std::string, std::string>>* Json2Pairs(const Json::Value& json)
    {
        if (json.isNull())
            return nullptr;

        std::vector<std::pair<std::string, std::string>>* pairs = new std::vector<std::pair<std::string, std::string>>();
        for (auto iter = json.begin(); iter != json.end(); ++ iter)
            pairs->emplace(pairs->end(), std::make_pair<std::string, std::string>(iter.name(), json[iter.name()].asString()));
        return pairs;
    }

    std::vector<std::pair<std::string, std::string>>* JsonPath2Pairs(const std::string& path)
    {
        std::vector<std::pair<std::string, std::string>>* pairs = nullptr;
        std::string* jsonStr = getStringFromFile(path);
        Json::Value json;
        Json::Reader jsonReader;
        if (jsonReader.parse(*jsonStr, json))
        {
            delete jsonStr;
            pairs = Json2Pairs(json);
        }
        return pairs;
    }

}