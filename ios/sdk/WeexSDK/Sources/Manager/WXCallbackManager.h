/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#import <Foundation/Foundation.h>

@interface WXCallbackManager : NSObject

+ (instancetype)sharedManager;

- (void)clearActions:(NSString*)instanceID;

- (void)registerComponent:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event callback:(void(^)(NSString*, NSString*, NSString*, NSDictionary*))callback;
- (void)cancelRegister:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event;
- (void)postToComponent:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event params:(NSDictionary*)params;

@end

