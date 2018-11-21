/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#ifndef WEEX_PROJECT_JSON_PARSER_H
#define WEEX_PROJECT_JSON_PARSER_H

#import <Foundation/Foundation.h>

@interface JsonParser : NSObject

+ (BOOL)parse:(NSString*)jsonLayout withStyles:(NSString*)jsonStyle asInstanceID:(NSString*)instanceId error:(NSError**)err;
+ (BOOL)parseFile:(NSString*)jsonLayoutPath withStylesFile:(NSString*)jsonStylePath asInstanceID:(NSString*)instanceId error:(NSError**)err;

@end

#endif // WEEX_PROJECT_JSON_PARSER_H
