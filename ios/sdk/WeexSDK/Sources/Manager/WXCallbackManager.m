/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#import "WXCallbackManager.h"

@implementation WXCallbackManager
{
    NSMutableDictionary* _callbacks;
}

+ (instancetype)sharedManager
{
    static id _sharedInstance = nil;
    static dispatch_once_t oncePredicate;
    dispatch_once(&oncePredicate, ^{
        _sharedInstance = [[WXCallbackManager alloc] init];
    });
    return _sharedInstance;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _callbacks = [NSMutableDictionary dictionary];
    }
    return self;
}

- (NSMutableDictionary*)getDictionary:(NSString*)instanceID withEvent:(NSString*)event
{
    id key = [[instanceID stringByAppendingString:@"@"] stringByAppendingString:event];
    if ([_callbacks objectForKey:key] == nil) {
        _callbacks[key] = [NSMutableDictionary dictionary];
    }
    return _callbacks[key];
}

- (void)clearActions:(NSString*)instanceID
{
    NSString* key = [instanceID stringByAppendingString:@"@"];
    NSMutableArray* pendingArray = [NSMutableArray array];
    for (id k in _callbacks) {
        if ([k hasPrefix:key]) {
            [pendingArray addObject:k];
        }
    }
    for (id k in pendingArray) {
        [_callbacks removeObjectForKey:k];
    }
}

- (void)registerComponent:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event callback:(void(^)(NSString*, NSString*, NSString*, NSDictionary*))callback
{
    NSMutableDictionary* callbacks = [self getDictionary:instanceID withEvent:event];
    callbacks[ref] = callback;
}

- (void)cancelRegister:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event
{
    NSMutableDictionary* callbacks = [self getDictionary:instanceID withEvent:event];
    [callbacks removeObjectForKey:ref];
}

- (void)postToComponent:(NSString*)ref forInstance:(NSString*)instanceID ofEvent:(NSString*)event params:(NSDictionary*)params
{
    NSMutableDictionary* callbacks = [self getDictionary:instanceID withEvent:event];
    if ([callbacks objectForKey:ref] != nil) {
        void(^callback)(NSString*, NSString*, NSString*, NSDictionary*) = [callbacks objectForKey:ref];
        callback(instanceID, ref, event, params);
    }
}

@end

