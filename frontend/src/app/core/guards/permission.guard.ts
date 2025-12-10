import {inject} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {map} from 'rxjs';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {ToastService} from '../services/toast.service';
import {ToastTypeEnum} from '../../shared/types/toast';

export const permissionGuard = (requiredPermission: UserPermissionsEnum): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const toast = inject(ToastService);

    return authService.checkAuthStatus().pipe(
      map(_ => {
        if (authService.hasPermission(requiredPermission)) {
          return true;
        }

        toast.show(`You do not have permission to view "${route.url}"`, ToastTypeEnum.ERROR);
        router.navigate(['']);
        return false;
      })
    );
  };
};
