/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#import "JsonParser.h"
#import "WXComponent.h"
#import "WXSDKManager.h"
#import "WXComponentManager.h"

@implementation JsonParser

+ (BOOL)doParse:(NSDictionary*)layoutDict withStyles:(NSDictionary*)styleDict asInstanceID:(NSString*)instanceId withParent:(NSString*)parent atIndex:(int)index
{
    BOOL ret = NO;
    WXSDKInstance* instance = [WXSDKManager instanceForID:instanceId];
    WXComponentManager* manager = [instance componentManager];
    [manager startComponentTasks];
    NSString* view = layoutDict[@"view"];
    if (view) {
        NSString* vid = (parent == nil ? WX_SDK_ROOT_REF : layoutDict[@"id"]);
        if (!vid) {
            vid = [NSString stringWithFormat:@"%@ANON_%@%d",
                   parent ? [NSString stringWithFormat:@"%@@", parent] : @"",
                   view, index];
        }
        
        const NSArray* RESERVED_ATTR_KEYS = [NSArray arrayWithObjects:@"view", @"id", @"class", @"style", @"subviews", nil];
        BOOL (^isReservedAttr)(NSString*) = ^(NSString* name) {
            for (NSString* key in RESERVED_ATTR_KEYS) {
                if ([name isEqualToString:key]) {
                    return YES;
                }
            }
            return NO;
        };
        
        void (^applyStyles)(NSDictionary*, NSMutableDictionary*, NSString*) = ^(NSDictionary* srcStyle, NSMutableDictionary* destStyle, NSString* pseudo) {
            for (NSString* key in srcStyle) {
                NSString* newKey = (pseudo && ![pseudo isEqualToString:@""]) ? [NSString stringWithFormat:@"%@%@", key, pseudo] : key;
                [destStyle setObject:srcStyle[key] forKey:newKey];
            }
        };
        
        void (^applyPseudoStyles)(NSString*, NSDictionary*, NSMutableDictionary*) = ^(NSString* clsname, NSDictionary* totalStyle, NSMutableDictionary* destStyle) {
            for (NSString* key in totalStyle) {
                if ([key isEqualToString:clsname] || [key hasPrefix:[NSString stringWithFormat:@"%@:", clsname]]) {
                    NSString* pseudo = nil;
                    NSInteger loc = [key rangeOfString:@":"].location;
                    if (loc != NSNotFound) {
                        pseudo = [key substringWithRange:NSMakeRange(loc, key.length - loc)];
                    }
                    applyStyles(totalStyle[key], destStyle, pseudo);
                }
            }
        };
        
        NSMutableArray* event = [NSMutableArray array];
        NSMutableDictionary* attr = [NSMutableDictionary dictionary];
        for (NSString* key in layoutDict) {
            if (!isReservedAttr(key)) {
                if ([key hasPrefix:@"@"]) {
                    [event addObject:key];
                } else {
                    [attr setObject:layoutDict[key] forKey:key];
                }
            }
        }
        
        NSMutableDictionary* style = [NSMutableDictionary dictionary];
        // apply class styles
        id clazz = layoutDict[@"class"];
        if (clazz) {
            if ([clazz isKindOfClass:[NSArray class]]) {
                for (NSString* clsname in clazz) {
                    applyPseudoStyles(clsname, styleDict, style);
                }
            } else if ([clazz isKindOfClass:[NSString class]]) {
                applyPseudoStyles(clazz, styleDict, style);
            }
        }
        // overrided styles
        applyStyles(layoutDict[@"style"], style, nil);
        
        NSMutableDictionary* componentData = [NSMutableDictionary dictionary];
        [componentData setObject:vid forKey:@"ref"];
        [componentData setObject:view forKey:@"type"];
        [componentData setObject:attr forKey:@"attr"];
        [componentData setObject:event forKey:@"event"];
        [componentData setObject:style forKey:@"style"];
        if (parent == nil) {
            [manager createRoot:componentData];
        } else {
            [manager addComponent:componentData toSupercomponent:parent atIndex:index appendingInTree:NO];
        }
        
        // handle subviews
        id subviews = layoutDict[@"subviews"];
        int subIndex = 0;
        if (subviews) {
            for (NSDictionary* subviewLayout in subviews) {
                [self doParse:subviewLayout withStyles:styleDict asInstanceID:instanceId withParent:vid atIndex:subIndex++];
            }
        }
        ret = YES;
    }
    return ret;
}

+ (BOOL)parse:(NSString*)jsonLayout withStyles:(NSString*)jsonStyle asInstanceID:(NSString*)instanceId error:(NSError**)err
{
    NSData* jsonLayoutData = [jsonLayout dataUsingEncoding:NSUTF8StringEncoding];
    NSData* jsonStyleData = [jsonStyle dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary* layoutDict = [NSJSONSerialization JSONObjectWithData:jsonLayoutData options:NSJSONReadingMutableContainers error:err];
    if (!*err) {
        NSDictionary* styleDict = [NSJSONSerialization JSONObjectWithData:jsonStyleData options:NSJSONReadingMutableContainers error:err];
        if (!*err) {
            return [self doParse:layoutDict withStyles:styleDict asInstanceID:instanceId withParent:nil atIndex:0];
        }
    }
    return NO;
}

+ (BOOL)parseFile:(NSString*)jsonLayoutPath withStylesFile:(NSString*)jsonStylePath asInstanceID:(NSString*)instanceId error:(NSError**)err
{
    NSString* jsonLayout = [NSString stringWithContentsOfFile:jsonLayoutPath encoding:NSUTF8StringEncoding error:err];
    NSString* jsonStyle = [NSString stringWithContentsOfFile:jsonStylePath encoding:NSUTF8StringEncoding error:err];
    if (!*err) {
        return [self parse:jsonLayout withStyles:jsonStyle asInstanceID:instanceId error:err];
    }
    return NO;
}

@end
